package com.github.crispyteam

import com.github.crispyteam.interpret.Interpreter
import com.github.crispyteam.tokenize.Token
import java.io.File
import java.nio.charset.Charset
import java.util.*

const val version = "0.1"
private var hadError = false
private lateinit var replInterpreter: Interpreter

fun main(args: Array<String>) =
        when {
            args.isEmpty() -> runInteractive()
            args.size == 1 -> runFile(args[0])
            else -> println("Usage: klox [path to script]")
        }

fun reportError(line: Int, msg: String) {
    System.err.println("[Error line: $line]: $msg")
}

fun reportError(token: Token, sourceLine: String, msg: String) {
    System.err.println("[Error line: ${token.line}]: $msg")
    System.err.println(sourceLine) // TODO arrow
}

fun runFile(fileName: String) {
    val file = File(fileName)
    val sourceCode = file.readText(Charset.defaultCharset())

    run(sourceCode)

    if (hadError) return
}

fun runInteractive() {
    println("Klox interactive shell version $version")

    val scanner = Scanner(System.`in`)
    replInterpreter = Interpreter()

    while (true) {
        print(">>> ")
        run(scanner.nextLine())
        hadError = false
    }
}

fun run(program: String) {
    replInterpreter.interpret(program)
}
