package com.github.crispyteam

import com.github.crispyteam.interpret.Interpreter
import com.github.crispyteam.tokenize.Token
import java.io.File
import java.nio.charset.Charset
import java.util.*
import kotlin.system.exitProcess

const val version = "0.1"
private var hadError = false

private val interpreter = Interpreter()
internal var inRepl = false
internal var replLine = 1
private var prompt = ">>> "

private var openParen = 0
private var openBrace = 0
private var openBracket = 0

fun main(args: Array<String>) =
        when {
            args.isEmpty() -> runInteractive()
            args.size == 1 -> runFile(args[0])
            else -> println("Usage: crispy [path to script]")
        }

fun reportError(line: Int, msg: String) {
    System.err.println("[Error line: $line]: $msg")
}

fun reportError(token: Token, msg: String) {
    System.err.println("[Error line: ${token.line}]: $msg. ${System.lineSeparator()}")

    val offset = " ".repeat(4)
    val lines = interpreter.sourceLines()
    val line = token.line - 1
    val start = token.startPos - 1

    val position = start - lines.subList(0, line).map { it.length + 1 }.sum() + token.lexeme.length

    System.err.println(offset + lines[line])
    System.err.println("$offset${" ".repeat(position)}^")
}

fun runFile(fileName: String) {
    val file = File(fileName)
    val sourceCode = file.readText(Charset.defaultCharset())

    run(sourceCode)

    if (hadError) return
}

fun runInteractive() {
    inRepl = true
    println("Crispy interactive shell version $version")

    val scanner = Scanner(System.`in`)
    var code = ""

    while (true) {
        print(prompt)
        try {
            val line = scanner.nextLine()

            if (line.isBlank()) continue

            code += line
            analyse(line)
            prompt = if (openBrace == 0 && openBracket == 0 && openParen == 0) {
                run(code)
                code = ""
                ">>> "
            } else {
                "... "
            }
            replLine++
        } catch (e: RuntimeException) {
            // scanner throws an annoying exception when repl is quit with EOF char
            println(e.message) // TODO remove when bugfree
            exitProcess(0)
        }

        hadError = false
    }
}

/**
 * Updates the number of open Parentheses, Brackets and Braces.
 */
private fun analyse(line: String) {
    openParen += line.filter { it == '(' }.count() - line.filter { it == ')' }.count()
    openBrace += line.filter { it == '{' }.count() - line.filter { it == '}' }.count()
    openBracket += line.filter { it == '[' }.count() - line.filter { it == ']' }.count()
}

fun run(program: String) {
    interpreter.interpret(program)
}
