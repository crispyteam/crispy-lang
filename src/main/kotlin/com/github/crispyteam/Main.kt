package com.github.crispyteam

import com.github.crispyteam.interpret.Interpreter
import com.github.crispyteam.tokenize.Token
import java.io.File
import java.nio.charset.Charset
import java.util.*
import kotlin.system.exitProcess

const val version = "0.1"
private const val prompt = ">>> "

private var hadError = false
private val interpreter = Interpreter()
internal var inRepl = false
internal var replLine = 1

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
    val lineNum = if (inRepl) 0 else 1
    val lines = interpreter.sourceLines()

    val linesBefore = lines.subList(0, lineNum).map { it.length }.sum() + if (inRepl) 0 else 1

    System.err.println(offset + lines[token.line - lineNum])
    System.err.println("$offset${" ".repeat(token.startPos - linesBefore)}^")
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

    while (true) {
        print(prompt)
        try {
            val line = scanner.nextLine()
            run(line)
            replLine++
        } catch (e: RuntimeException) {
            // scanner throws an annoying exception when repl is quit with EOF char
            println(e.message) // TODO remove when bugfree
            exitProcess(0)
        }

        hadError = false
    }
}

fun run(program: String) {
    interpreter.interpret(program)
}
