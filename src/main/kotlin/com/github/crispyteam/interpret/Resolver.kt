package com.github.crispyteam.interpret

import com.github.crispyteam.parse.Expr
import com.github.crispyteam.parse.Stmt
import com.github.crispyteam.tokenize.Token
import java.util.*
import kotlin.collections.HashMap

class ResolveError(val token: Token, msg: String) : RuntimeException(msg)

class Resolver(private val interpreter: Interpreter) : Stmt.Visitor<Unit>, Expr.Visitor<Unit> {
    private val scopes = Stack<MutableMap<String, Boolean>>()
    private var inMethod = false

    fun resolve(statements: List<Stmt>) {
        statements.forEach { resolve(it) }
    }

    private fun resolve(stmt: Stmt) {
        stmt.accept(this)
    }

    private fun resolve(expr: Expr) {
        expr.accept(this)
    }

    private fun beginScope() {
        scopes.push(HashMap())
    }

    private fun endScope() {
        scopes.pop()
    }

    private fun declare(name: Token) {
        if (scopes.empty()) return

        val scope = scopes.peek()
        if (scope.containsKey(name.lexeme)) {
            throw ResolveError(name, "Variable already defined in scope")
        }

        // declared, but not initialized
        scope[name.lexeme] = false
    }

    private fun define(name: Token) {
        if (scopes.empty()) return
        // Mark as initialized and ready to use
        scopes.peek()[name.lexeme] = true
    }

    private fun resolveLocal(name: Token) {
        for (i in scopes.indices.reversed()) { // TODO check if easier when reversed().indices
            if (scopes[i].containsKey(name.lexeme)) {
                interpreter.resolve(name, scopes.size - 1 - i)
                return
            }
        }
    }

    override fun visitExpression(expressionStmt: Stmt.Expression) {
        resolve(expressionStmt.expr)
    }

    override fun visitReturn(returnStmt: Stmt.Return) {
        if (returnStmt.expr != null) {
            resolve(returnStmt.expr)
        }
    }

    override fun visitBreak(breakStmt: Stmt.Break) {

    }

    override fun visitContinue(continueStmt: Stmt.Continue) {

    }

    override fun visitBlock(blockStmt: Stmt.Block) {
        beginScope()
        blockStmt.statements.forEach { resolve(it) }
        endScope()
    }

    override fun visitIf(ifStmt: Stmt.If) {
        resolve(ifStmt.condition)
        resolve(ifStmt.thenBlock)
        if (ifStmt.elseBlock != null) resolve(ifStmt.elseBlock)
    }

    override fun visitValDecl(valdeclStmt: Stmt.ValDecl) {
        declare(valdeclStmt.name)
        resolve(valdeclStmt.value)
        define(valdeclStmt.name)
    }

    override fun visitVarDecl(vardeclStmt: Stmt.VarDecl) {
        declare(vardeclStmt.name)
        if (vardeclStmt.value != null) {
            resolve(vardeclStmt.value)
            define(vardeclStmt.name)
        }
    }

    override fun visitWhile(whileStmt: Stmt.While) {
        resolve(whileStmt.condition)
        resolve(whileStmt.block)
    }

    override fun visitSet(setStmt: Stmt.Set) {
        resolve(setStmt.obj)
        resolve(setStmt.value)
    }

    override fun visitSetBraces(setbracesStmt: Stmt.SetBraces) {
        resolve(setbracesStmt.key)
        resolve(setbracesStmt.obj)
        resolve(setbracesStmt.value)
    }

    override fun visitAssignment(assignmentStmt: Stmt.Assignment) {
        resolve(assignmentStmt.value)
        resolveLocal(assignmentStmt.name)
    }

    override fun visitIncrement(incrementStmt: Stmt.Increment) {

    }

    override fun visitDecrement(decrementStmt: Stmt.Decrement) {

    }

    override fun visitBinary(binaryExpr: Expr.Binary) {
        resolve(binaryExpr.left)
        resolve(binaryExpr.right)
    }

    override fun visitUnary(unaryExpr: Expr.Unary) {
        resolve(unaryExpr.expr)
    }

    override fun visitLambda(lambdaExpr: Expr.Lambda) {
        beginScope()
        lambdaExpr.parameters.forEach {
            declare(it)
            define(it)
        }
        resolve(lambdaExpr.body)
        endScope()
    }

    override fun visitCall(callExpr: Expr.Call) {
        resolve(callExpr.callee)
        callExpr.arguments.forEach {
            resolve(it)
        }
    }

    override fun visitGet(getExpr: Expr.Get) {
        resolve(getExpr.obj)
    }

    override fun visitAccess(accessExpr: Expr.Access) {
        resolve(accessExpr.obj)
        resolve(accessExpr.key)
    }

    override fun visitLiteral(literalExpr: Expr.Literal) {

    }

    override fun visitVariable(variableExpr: Expr.Variable) {
        if (!scopes.empty() && scopes.peek()[variableExpr.name.lexeme] == false) {
            throw ResolveError(variableExpr.name, "Cannot read local variable in its own initializer")
        }

        resolveLocal(variableExpr.name)
    }

    override fun visitGrouping(groupingExpr: Expr.Grouping) {
        resolve(groupingExpr.expr)
    }

    override fun visitDictionary(dictionaryExpr: Expr.Dictionary) {
        dictionaryExpr.pairs.forEach {
            resolve(it.first)
            resolve(it.second)
        }
    }

    override fun visitCrispyList(crispylistExpr: Expr.CrispyList) {
        crispylistExpr.items.forEach {
            resolve(it)
        }
    }
}