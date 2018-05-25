package com.github.crispyteam.interpret

import com.github.crispyteam.tokenize.Token

private data class Variable(val value: Any?, val assignable: Boolean) {
    override fun toString(): String = value?.toString() ?: ""
}

class Environment(val outer: Environment?) {
    private val values = HashMap<String, Variable>()

    class RedefinitionError : RuntimeException()
    class AssignmentError(val token: Token, msg: String) : RuntimeException(msg)

    fun get(key: Token): Any? {
        val value = values[key.lexeme]

        if (value == null) {
            if (outer != null) {
                return outer.get(key)
            }
            throw RuntimeError(key, "Undefined variable '${key.lexeme}'")
        }

        return value.value
    }

    fun define(key: String, value: Any?, assignable: Boolean) {
        if (!values.containsKey(key)) {
            values[key] = Variable(value, assignable)
        } else {
            throw RedefinitionError()
        }
    }

    fun assign(key: Token, value: Any?) {
        val result = values[key.lexeme]
        if (result != null) {
            if (result.assignable) {
                values[key.lexeme] = Variable(value, true)
                return
            }
            throw AssignmentError(key, "Cannot reassign value '${key.lexeme}'")
        }

        if (outer != null) {
            outer.assign(key, value)
        } else {
            throw AssignmentError(key, "Undefined variable '${key.lexeme}'")
        }
    }

    fun getAt(distance: Int, name: String): Any? =
            ancestor(distance).values[name]?.value

    fun assignAt(distance: Int, name: Token, value: Any?) {
        ancestor(distance).values[name.lexeme] = Variable(value, true)
    }

    private fun ancestor(distance: Int): Environment {
        var env = this
        for (i in 0 until distance) env = env.outer!! // TODO safe
        return env
    }
}