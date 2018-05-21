package com.github.crispyteam.interpret

fun getStdLib(): Map<String, CrispyCallable> {
    return mapOf(
            "println" to object : CrispyCallable {
                override fun arity(): Int = 1

                override fun call(interpreter: Interpreter, args: List<Variable>): Any? {
                    println(stringify(args[0]))
                    return args[0]
                }
            }
    )
}