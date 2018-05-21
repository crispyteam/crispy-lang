package com.github.crispyteam.interpret

data class Variable(val value: Any?, val assignable: Boolean) {
    fun literal(): Any? {
        if (value is Variable) return value.literal()
        return value
    }

    override fun toString(): String = value?.toString() ?: ""
}