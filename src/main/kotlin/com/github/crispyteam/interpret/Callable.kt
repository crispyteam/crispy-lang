package com.github.crispyteam.interpret

import com.github.crispyteam.parse.Expr

interface CrispyCallable {
    fun arity(): Int
    fun call(interpreter: Interpreter, args: List<Variable>): Any?
}

class CrispyFunction(
        private var closure: Environment,
        private val declaration: Expr.Lambda
) : CrispyCallable {
    init {
        declaration.parameters.forEach {
            closure.define(it.literal.toString(), null, true)
        }
    }

    internal fun bind(self: Map<String, Any?>): CrispyFunction {
        val env = Environment(closure)
        env.define("self", self, false)
        return CrispyFunction(env, declaration)
    }

    override fun arity(): Int =
            declaration.parameters.size

    override fun call(interpreter: Interpreter, args: List<Variable>): Any? {
        closure = Environment(closure)
        declaration.parameters.withIndex().forEach { (i, it) ->
            closure.define(it.literal.toString(), args[i], false)
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