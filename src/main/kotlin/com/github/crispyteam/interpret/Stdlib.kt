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
            },
            "sleep" to object : CrispyCallable {
                override fun arity(): Int = 1

                override fun call(interpreter: Interpreter, args: List<Variable>): Any? {
                    val millis = args[0].literal() as Double
                    Thread.sleep(millis.toLong())
                    return millis
                }
            },
            "input" to object : CrispyCallable {
                override fun call(interpreter: Interpreter, args: List<Variable>): Any? {
                    return readLine()
                }

                override fun arity(): Int = 0
            },
            "p_input" to object : CrispyCallable {
                override fun arity(): Int = 1

                override fun call(interpreter: Interpreter, args: List<Variable>): Any? {
                    val prompt = stringify(args[0])
                    print(prompt)
                    return readLine()
                }
            }
    )
}