package com.github.crispyteam.interpret

import kotlin.system.exitProcess

fun getStdLib(): Map<String, CrispyCallable> {
    return mapOf(
            "println" to object : CrispyCallable {
                override fun arity(): Int = 1

                override fun call(interpreter: Interpreter, vararg args: Any?): String {
                    val s = stringify(args[0])
                    println(s)
                    return s
                }
            },
            "exit" to object : CrispyCallable {
                override fun arity(): Int = 1

                override fun call(interpreter: Interpreter, vararg args: Any?) {
                    val variable = args[0] as Double
                    exitProcess(variable.toInt())
                }
            },
            "len" to object : CrispyCallable {
                override fun arity(): Int = 1

                override fun call(interpreter: Interpreter, vararg args: Any?): Double {
                    val list = args[0] as Collection<*>
                    return list.size.toDouble()
                }
            },
            "sleep" to object : CrispyCallable {
                override fun arity(): Int = 1

                override fun call(interpreter: Interpreter, vararg args: Any?): Any? {
                    val millis = args[0] as Double
                    Thread.sleep(millis.toLong())
                    return millis
                }
            },
            "input" to object : CrispyCallable {
                override fun call(interpreter: Interpreter, vararg args: Any?): Any? {
                    return readLine()
                }

                override fun arity(): Int = 0
            },
            "p_input" to object : CrispyCallable {
                override fun arity(): Int = 1

                override fun call(interpreter: Interpreter, vararg args: Any?): Any? {
                    val prompt = stringify(args[0])
                    print(prompt)
                    return readLine()
                }
            },
            "clock" to object : CrispyCallable {
                override fun arity(): Int = 0

                override fun call(interpreter: Interpreter, vararg args: Any?): Any? {
                    return System.currentTimeMillis().toDouble()
                }
            },
            "str" to object : CrispyCallable {
                override fun arity(): Int = 1

                override fun call(interpreter: Interpreter, vararg args: Any?): Any? =
                        stringify(args[0])
            }
    )
}