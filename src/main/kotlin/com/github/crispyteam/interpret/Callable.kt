package com.github.crispyteam.interpret

import com.github.crispyteam.parse.Expr

interface CrispyCallable {
    fun arity(): Int
    fun call(interpreter: Interpreter, vararg args: Any?): Any?
}

class CrispyFunction(
        private var closure: Environment,
        private val declaration: Expr.Lambda
) : CrispyCallable {
    private var isMethod = false

    init {
        declaration.parameters.forEach {
            closure.define(it.lexeme, null, true)
        }
    }

    internal fun bind(self: Map<String, Any?>): CrispyFunction {
        isMethod = true
        closure.define("self", self, false)
        return CrispyFunction(closure, declaration)
    }

    override fun arity(): Int =
            declaration.parameters.size

    override fun call(interpreter: Interpreter, vararg args: Any?): Any? {
        closure = Environment(closure)
        declaration.parameters.withIndex().forEach { (i, it) ->
            closure.define(it.lexeme, args[i], true)
        }

        try {
            interpreter.executeBlock(closure, declaration.body)
        } catch (returnValue: Return) {
            return returnValue.value
        }

        return null
    }

    override fun toString(): String {
        return "<function (${declaration.parameters.joinToString(", ") { it.literal.toString() }})>"
    }


}