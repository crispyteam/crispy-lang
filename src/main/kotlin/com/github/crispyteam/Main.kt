package com.github.crispyteam

import com.github.crispyteam.tokenize.Lexer
import com.github.crispyteam.tokenize.Token
import java.io.File
import java.nio.charset.Charset
import java.util.*

const val version = "0.1"
var hadError = false

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

    while (true) {
        print(">>> ")
        run(scanner.nextLine())
        hadError = false
    }
}

fun run(program: String) {
    val lexer = Lexer(program)
    val tokens = lexer.lex()

    println(tokens)
}
