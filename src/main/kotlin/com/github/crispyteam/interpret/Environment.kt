package com.github.crispyteam.interpret

import com.github.crispyteam.tokenize.Token

class Environment(private val outer: Environment?) {
    private val values = HashMap<String, Variable>()

    class RedefinitionError : RuntimeException()
    class AssignmentError(val token: Token, msg: String) : RuntimeException(msg)

    fun get(key: Token): Variable? {
        val value = values[key.lexeme]

        if (value == null) {
            if (outer != null) {
                return outer.get(key)
            }
            throw RuntimeError(key, "Undefined variable '${key.lexeme}'")
        }

        return value
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

}