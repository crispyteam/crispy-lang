package com.github.crispyteam.interpret

import com.github.crispyteam.tokenize.Token

class Environment(private val outer: Environment?) {
    private val values = HashMap<String, Variable>()

    class RedefinitionError : RuntimeException()
    class AssignmentError(msg: String) : RuntimeException(msg)

    fun get(key: Token): Any? {
        val value = values[key.literal]

        if (value == null) {
            if (outer != null) {
                return outer.get(key)
            }
            throw RuntimeError(key, "Undefined variable '${key.literal}'")
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

    fun assign(key: String, value: Any?) {
        val result = values[key]
        if (result != null) {
            if (result.assignable) {
                values[key] = Variable(value, true)
                return
            }
            throw AssignmentError("Cannot reassing value '$key'")
        }

        throw AssignmentError("Undefined variable '$key'")
    }
}