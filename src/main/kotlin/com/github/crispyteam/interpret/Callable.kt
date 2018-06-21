package com.github.crispyteam.interpret

import com.github.crispyteam.parse.Expr
import com.github.crispyteam.parse.Stmt

interface CrispyCallable {
    fun arity(): Int
    fun call(interpreter: Interpreter, vararg args: Any?): Any?
}

class CrispyFunction(
        private var closure: Environment,
        private val declaration: Expr.Lambda
) : CrispyCallable {
    override fun arity(): Int =
            declaration.parameters.size

    override fun call(interpreter: Interpreter, vararg args: Any?): Any? {
        closure = Environment(closure)
        declaration.parameters.withIndex().forEach { (i, it) ->
            closure.define(it.lexeme, args[i], true)
        }

        try {
            when (declaration.body) {
                is Stmt.Block -> interpreter.executeLambdaBlock(closure, declaration.body)
                else -> interpreter.executeLambdaSingle(closure, declaration.body)
            }
        } catch (returnValue: Return) {
            return returnValue.value
        }

        return null
    }

    override fun toString(): String {
        return "<function (${declaration.parameters.joinToString(", ") { it.literal.toString() }})>"
    }


}