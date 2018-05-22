package com.github.crispyteam.interpret

import kotlin.system.exitProcess

fun getStdLib(): Map<String, CrispyCallable> {
    return mapOf(
            "println" to object : CrispyCallable {
                override fun arity(): Int = 1

                override fun call(interpreter: Interpreter, args: List<Variable>): String {
                    val s = stringify(args[0])
                    println(s)
                    return s
                }
            },
            "exit" to object : CrispyCallable {
                override fun arity(): Int = 1

                override fun call(interpreter: Interpreter, args: List<Variable>) {
                    val variable = args[0].literal() as Double
                    exitProcess(variable.toInt())
                }
            },
            "len" to object : CrispyCallable {
                override fun arity(): Int = 1

                override fun call(interpreter: Interpreter, args: List<Variable>): Double {
                    val list = args[0].literal() as Collection<*>
                    return list.size.toDouble()
                }
            }
    )
}