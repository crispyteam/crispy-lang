package com.github.crispyteam.interpret

import com.github.crispyteam.parse.Expr
import com.github.crispyteam.parse.ParseError
import com.github.crispyteam.parse.Parser
import com.github.crispyteam.parse.Stmt
import com.github.crispyteam.tokenize.Lexer
import com.github.crispyteam.tokenize.Token


class RuntimeError(token: Token, msg: String) :
        RuntimeException("[Error line: ${token.line}]: $msg")

class Return(val value: Any?) :
        RuntimeException()

internal fun stringify(value: Variable): String {
    return when (value.value) {
        null -> "nil"
        is Double -> {
            var text = value.toString()
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length - 2)
            }
            text
        }
        is Variable -> stringify(value.value)
        else -> value.toString()
    }
}

class Interpreter : Stmt.Visitor<Unit>, Expr.Visitor<Any?> {
    private val globals = Environment(null)
    private var environment = globals

    private var sourceCode = ""

    init {
        getStdLib().entries.forEach { globals.define(it.key, it.value, false) }
    }

    fun interpret(sourceCode: String) {
        this.sourceCode = sourceCode
        val parser = Parser(Lexer(sourceCode))
        parser.parse().forEach { execute(it) }
    }

    private fun evaluate(expr: Expr): Any? =
            expr.accept(this)

    private fun execute(stmt: Stmt) {
        stmt.accept(this)
    }

    private fun lexeme(token: Token): String =
            sourceCode.substring(token.startPos, token.endPos)

    private fun isTruthful(obj: Any?): Boolean {
        if (obj == null) return false
        if (obj is Boolean) return obj

        return true
    }

    internal fun executeBlock(environment: Environment, stmt: Stmt) {
        val previous = this.environment
        this.environment = environment

        try {
            when (stmt) {
                is Stmt.Block -> stmt.statements.forEach { execute(it) }
                is Stmt.Expression -> throw Return(evaluate(stmt.expr))
            }
        } finally {
            this.environment = previous
        }
    }

    override fun visitExpression(expressionStmt: Stmt.Expression) {
        evaluate(expressionStmt.expr)
    }

    override fun visitReturn(returnStmt: Stmt.Return) {
        if (returnStmt.expr != null)
            throw Return(evaluate(returnStmt.expr))
    }

    override fun visitBreak(breakStmt: Stmt.Break) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitContinue(continueStmt: Stmt.Continue) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitBlock(blockStmt: Stmt.Block) {
        blockStmt.statements.forEach { execute(it) }
    }

    override fun visitIf(ifStmt: Stmt.If) {
        if (isTruthful(evaluate(ifStmt.condition))) {
            execute(ifStmt.thenBlock)
        } else if (ifStmt.elseBlock != null) {
            execute(ifStmt.elseBlock)
        }
    }

    override fun visitValDecl(valdeclStmt: Stmt.ValDecl) {
        val name = valdeclStmt.name.literal as? String ?: throw RuntimeError(valdeclStmt.name, "Type mismatch")
        val value = evaluate(valdeclStmt.value)
        try {
            environment.define(name, value, false)
        } catch (err: Environment.RedefinitionError) {
            throw RuntimeError(valdeclStmt.name, "value already defined in scope")
        }
    }

    override fun visitVarDecl(vardeclStmt: Stmt.VarDecl) {
        val name = vardeclStmt.name.literal as? String ?: throw RuntimeError(vardeclStmt.name, "Type mismatch")
        val value = if (vardeclStmt.value != null)
            evaluate(vardeclStmt.value)
        else null

        try {
            environment.define(name, value, true)
        } catch (err: Environment.RedefinitionError) {
            throw RuntimeError(vardeclStmt.name, "variable already defined in scope")
        }
    }

    override fun visitAssignment(assignmentStmt: Stmt.Assignment) {
        val value = evaluate(assignmentStmt.value)
        try {
            environment.assign(lexeme(assignmentStmt.name), value)
        } catch (err: Environment.AssignmentError) {
            throw RuntimeError(assignmentStmt.name, err.message ?: "")
        }
    }

    override fun visitSet(setStmt: Stmt.Set) {
        val variable = evaluate(setStmt.obj) as? Variable
                ?: throw RuntimeError(setStmt.token, "Invalid syntax")

        val obj = variable.literal() as? MutableMap<String, Any?>
                ?: throw RuntimeError(setStmt.token, "Invalid target for set operation")

        var value = evaluate(setStmt.value)

        if (value is CrispyFunction) {
            value = value.bind(obj)
        }

        obj[lexeme(setStmt.key)] = Variable(value, true)
    }

    override fun visitIncrement(incrementStmt: Stmt.Increment) {
        val key = lexeme(incrementStmt.variable)
        val value = environment.get(incrementStmt.variable)
        environment.assign(key, (value as Variable).value as Double + 1) // TODO check type
    }

    override fun visitDecrement(decrementStmt: Stmt.Decrement) {
        val key = lexeme(decrementStmt.variable)
        val value = environment.get(decrementStmt.variable)
        environment.assign(key, (value as Variable).value as Double - 1) // TODO check type
    }

    override fun visitWhile(whileStmt: Stmt.While) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitBinary(binaryExpr: Expr.Binary): Any? {
        var left = evaluate(binaryExpr.left)
        if (left is Variable) left = left.literal()
        var right = evaluate(binaryExpr.right)
        if (right is Variable) right = right.literal()

        return when (left) {
            is Double -> when (right) {
                is Double -> left + right
                else -> throw RuntimeError(binaryExpr.operator, "Right expression must evaluate to a number")
            }
            is String -> left + stringify(Variable(right, true))
            else -> throw RuntimeError(binaryExpr.operator, "Left expression must evaluate to a string or a number")
        }
    }

    override fun visitUnary(unaryExpr: Expr.Unary): Any? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitLambda(lambdaExpr: Expr.Lambda): Any? =
            CrispyFunction(this.environment, lambdaExpr)

    override fun visitCall(callExpr: Expr.Call): Any? {
        val pair = evaluate(callExpr.callee) as? Variable
                ?: throw RuntimeError(callExpr.paren, "Error while resolving function name")

        val callee = pair.value as? CrispyCallable
                ?: throw RuntimeError(callExpr.paren, "Can only call functions")

        val args = callExpr.arguments
                .map { Variable(evaluate(it), true) }
                .toCollection(ArrayList())

        if (args.size != callee.arity()) {
            throw RuntimeError(callExpr.paren, "Invalid number of arguments. Expected ${callee.arity()}, " +
                    "but got ${args.size}")
        }

        return callee.call(this, args)
    }

    override fun visitGet(getExpr: Expr.Get): Any? {
        val variable = (evaluate(getExpr.obj) as? Variable) ?: throw ParseError(getExpr.token, "Invalid syntax")

        val obj = variable.literal() as? Map<*, *>
                ?: throw RuntimeError(getExpr.token, "Can only use '.' on Dictionaries")

        val key = getExpr.key.literal

        return obj[key]
    }

    override fun visitAccess(accessExpr: Expr.Access): Any? {
        val variable = (evaluate(accessExpr.obj) as? Variable) ?: throw ParseError(accessExpr.brace, "Invalid syntax")

        val obj = variable.literal() as? Map<*, *>
                ?: throw RuntimeError(accessExpr.brace, "Can only use '[...]' syntax on Dictionaries or lists")

        val key = evaluate(accessExpr.key)

        return obj[key]
    }

    override fun visitLiteral(literalExpr: Expr.Literal) =
            literalExpr.value

    override fun visitVariable(variableExpr: Expr.Variable): Any? =
            environment.get(variableExpr.name)

    override fun visitGrouping(groupingExpr: Expr.Grouping): Any? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitDictionary(dictionaryExpr: Expr.Dictionary): Any? {
        val dict = dictionaryExpr.pairs.map {
            evaluate(it.first) as String to Variable(evaluate(it.second), true)
        }.toMap(HashMap())

        dict.entries.forEach {
            val func = it.value.value
            if (func is CrispyFunction) {
                it.setValue(Variable(func.bind(dict), true))
            }
        }

        return dict
    }

    override fun visitCrispyList(crispylistExpr: Expr.CrispyList): List<Any> {
        return crispylistExpr.items
    }
}