package com.github.crispyteam.interpret

import com.github.crispyteam.inRepl
import com.github.crispyteam.parse.Expr
import com.github.crispyteam.parse.Parser
import com.github.crispyteam.parse.Stmt
import com.github.crispyteam.reportError
import com.github.crispyteam.tokenize.Lexer
import com.github.crispyteam.tokenize.Token
import com.github.crispyteam.tokenize.TokenType.*


class RuntimeError(val token: Token, msg: String) :
        RuntimeException(msg)

class Return(val value: Any?) : RuntimeException(null, null, false, false)
class Break : RuntimeException(null, null, false, false)
class Continue : RuntimeException(null, null, false, false)

internal fun stringify(value: Any?): String {
    return when (value) {
        null -> "nil"
        is Double -> {
            var text = value.toString()
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length - 2)
            }
            text
        }
        else -> value.toString()
    }
}

class Interpreter : Stmt.Visitor<Unit>, Expr.Visitor<Any?> {
    private val globals = Environment(null)
    private var environment = globals
    private val resolver = Resolver(this)

    private val distances = HashMap<Token, Int>()

    private var sourceCode = ""

    init {
        getStdLib().entries.forEach { globals.define(it.key, it.value, false) }
    }

    fun interpret(sourceCode: String) {
        this.sourceCode = if (inRepl)
            this.sourceCode + System.lineSeparator() + sourceCode
        else
            sourceCode

        val parser = Parser(Lexer(sourceCode))
        val statements = parser.parse()
        resolver.resolve(statements)

        try {
            statements.forEach { execute(it) }
        } catch (err: RuntimeError) {
            reportError(err.token, err.message ?: "Runtime Error")
        }
    }

    fun sourceLines(): List<String> =
            sourceCode.split("\n").toCollection(ArrayList())

    private fun evaluate(expr: Expr): Any? =
            expr.accept(this)

    private fun execute(stmt: Stmt) {
        stmt.accept(this)
    }

    fun resolve(name: Token, distance: Int) {
        distances[name] = distance
    }

    private fun lookupVariable(name: Token): Any? {
        val distance = distances[name]
        return if (distance != null) {
            environment.getAt(distance, name.lexeme)
        } else {
            globals.get(name)
        }
    }

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
        throw Break()
    }

    override fun visitContinue(continueStmt: Stmt.Continue) {
        throw Continue()
    }

    override fun visitBlock(blockStmt: Stmt.Block) {
        executeBlock(Environment(this.environment), blockStmt)
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
            environment.assign(assignmentStmt.name, value)
        } catch (err: Environment.AssignmentError) {
            throw RuntimeError(assignmentStmt.name, err.message ?: "")
        }
    }

    override fun visitSetBraces(setbracesStmt: Stmt.SetBraces) {
        val variable = evaluate(setbracesStmt.obj)

        val obj = variable as? CrispyDictionary
                ?: throw RuntimeError(setbracesStmt.token, "Invalid target for set operation")

        val key = evaluate(setbracesStmt.key) as? String
                ?: throw RuntimeError(setbracesStmt.token, "Can only use strings as index in [...] syntax")

        var value = evaluate(setbracesStmt.value)

        if (value is CrispyFunction) {
            value = value.bind(obj)
        }

        obj[key] = value
    }

    override fun visitSet(setStmt: Stmt.Set) {
        val variable = evaluate(setStmt.obj)

        val obj = variable as? CrispyDictionary
                ?: throw RuntimeError(setStmt.token, "Invalid target for set operation")

        var value = evaluate(setStmt.value)

        if (value is CrispyFunction) {
            value = value.bind(obj)
        }

        obj[setStmt.key.lexeme] = value
    }

    override fun visitIncrement(incrementStmt: Stmt.Increment) {
        val key = incrementStmt.variable
        val value = lookupVariable(incrementStmt.variable)
        try {
            environment.assign(key, value as Double + 1) // TODO check type
        } catch (err: Environment.AssignmentError) {
            throw RuntimeError(err.token, err.message ?: "Assignment Error")
        }
    }

    override fun visitDecrement(decrementStmt: Stmt.Decrement) {
        val key = decrementStmt.variable
        val value = lookupVariable(decrementStmt.variable)
        environment.assign(key, value as Double - 1) // TODO check type
    }

    override fun visitWhile(whileStmt: Stmt.While) {
        while (isTruthful(evaluate(whileStmt.condition))) {
            try {
                execute(whileStmt.block)
            } catch (cont: Continue) {
                continue
            } catch (brk: Break) {
                break
            }
        }
    }

    override fun visitBinary(binaryExpr: Expr.Binary): Any? {
        val left = evaluate(binaryExpr.left)
        val right = evaluate(binaryExpr.right)

        val op = binaryExpr.operator

        return when (op.type) {
            MINUS ->
                if (left is Double && right is Double) left - right
                else throw RuntimeError(op, "Both operands must be numbers")
            PERCENT ->
                if (left is Double && right is Double) left % right
                else throw RuntimeError(op, "Both operands must be numbers")
            STAR ->
                if (left is Double && right is Double) left * right
                else throw RuntimeError(op, "Both operands must be numbers")
            SLASH ->
                if (left is Double && right is Double) left / right
                else throw RuntimeError(op, "Both operands must be numbers")
            SMALLER ->
                if (left is Double && right is Double) left < right
                else throw RuntimeError(op, "Both operands must be numbers")
            GREATER ->
                if (left is Double && right is Double) left > right
                else throw RuntimeError(op, "Both operands must be numbers")
            SMALLER_EQUALS ->
                if (left is Double && right is Double) left <= right
                else throw RuntimeError(op, "Both operands must be numbers")
            GREATER_EQUALS ->
                if (left is Double && right is Double) left >= right
                else throw RuntimeError(op, "Both operands must be numbers")
            PLUS -> when (left) {
                is Double ->
                    if (right is Double) left + right
                    else throw RuntimeError(op, "Second operator must be a number")
                is String -> left + stringify(right)
                else -> throw RuntimeError(op, "Invalid first operand")
            }
            EQUALS_EQUALS -> when (left) {
                is Double ->
                    if (right is Double) left == right
                    else throw RuntimeError(op, "Second operand must be a number")
                is String ->
                    if (right is String) left == right
                    else throw RuntimeError(op, "Second operand must be a String")
                is Map<*, *> -> when (right) {
                    is Map<*, *> -> left == right
                    null -> false
                    else -> throw RuntimeError(op, "Invalid second operand")
                }
                else -> when (right) {
                    null -> left == null
                    else -> throw RuntimeError(op, "Invalid first operand")
                }
            }
            BANG_EQUALS -> when (left) {
                is Double ->
                    if (right is Double) left != right
                    else throw RuntimeError(op, "Second operand must be a number")
                is String ->
                    if (right is String) left != right
                    else throw RuntimeError(op, "Second operand must be a String")
                is Map<*, *> -> when (right) {
                    is Map<*, *> -> left != right
                    null -> true
                    else -> throw RuntimeError(op, "Invalid second operand")
                }
                else -> when (right) {
                    null -> left != null
                    else -> throw RuntimeError(op, "Invalid first operand")
                }
            }
            else -> throw  RuntimeError(op, "Invalid first operand for binary expression")
        }
    }

    override fun visitUnary(unaryExpr: Expr.Unary): Any? {
        val result = evaluate(unaryExpr.expr)

        return when (unaryExpr.operator.type) {
            MINUS -> {
                result as? Double
                        ?: throw RuntimeError(unaryExpr.operator, "'-' operator can only be used on numeric values")
                -result
            }
            BANG -> !isTruthful(result)
            else -> throw RuntimeError(unaryExpr.operator, "Invalid operand for operator")
        }
    }

    override fun visitLambda(lambdaExpr: Expr.Lambda): Any? =
            CrispyFunction(Environment(this.environment), lambdaExpr)

    override fun visitCall(callExpr: Expr.Call): Any? {
        val variable = evaluate(callExpr.callee)

        val callee = variable as? CrispyCallable
                ?: throw RuntimeError(callExpr.paren, "Can only call functions")

        val args = callExpr.arguments
                .map { evaluate(it) }
                .toTypedArray()

        if (args.size != callee.arity()) {
            throw RuntimeError(callExpr.paren, "Invalid number of arguments. Expected ${callee.arity()}, " +
                    "but got ${args.size}")
        }

        return callee.call(this, *args)
    }

    override fun visitGet(getExpr: Expr.Get): Any? {
        val variable = evaluate(getExpr.obj)

        val obj = variable as? CrispyDictionary
                ?: throw RuntimeError(getExpr.token, "Can only use '.' on Dictionaries")

        val key = getExpr.key.literal

        return obj[key]
    }

    override fun visitAccess(accessExpr: Expr.Access): Any? {
        val obj = evaluate(accessExpr.obj)

        val key = evaluate(accessExpr.key)

        return when (obj) {
            is Map<*, *> -> obj[key]
            is List<*> -> when (key) {
                is Double -> obj[key.toInt()]
                else -> throw RuntimeError(accessExpr.brace, "Can only use natural numbers as list index")
            }
            else -> throw RuntimeError(accessExpr.brace, "Can only use '[...]' syntax on Dictionaries or lists")
        }
    }

    override fun visitLiteral(literalExpr: Expr.Literal) =
            literalExpr.value

    override fun visitVariable(variableExpr: Expr.Variable): Any? =
            lookupVariable(variableExpr.name)

    override fun visitGrouping(groupingExpr: Expr.Grouping): Any? =
            evaluate(groupingExpr.expr)

    override fun visitDictionary(dictionaryExpr: Expr.Dictionary): CrispyDictionary {
        val dict = dictionaryExpr.pairs.map {
            evaluate(it.first) as String to evaluate(it.second)
        }.toMap(HashMap())

        dict.entries.forEach {
            val func = it.value
            if (func is CrispyFunction) {
                it.setValue(func.bind(dict))
            }
        }

        return dict
    }

    override fun visitCrispyList(crispylistExpr: Expr.CrispyList): List<Any?> {
        return crispylistExpr.items.map { evaluate(it) }.toList()
    }
}