package com.github.crispyteam.interpret

import kotlin.system.exitProcess

fun getStdLib(): Map<String, CrispyCallable> {
    return mapOf(
            "println" to object : CrispyCallable {
                override fun arity(): Int = 1

                override fun call(interpreter: Interpreter, args: List<Variable>): Any? {
                    println(stringify(args[0]))
                    return args[0]
                }
            },
            "exit" to object : CrispyCallable {
                override fun arity(): Int = 1

                override fun call(interpreter: Interpreter, args: List<Variable>): Any? {
                    val variable = args[0].literal() as Double
                    exitProcess(variable.toInt())
                }
            }
    )
}