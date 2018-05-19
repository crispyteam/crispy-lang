package com.github.crispyteam.tools

import java.io.PrintWriter

fun main(args: Array<String>) {
    defineAst("src/main/kotlin/com/github/crispyteam/parse", "Stmt", listOf(
            "Expression     = expr: Expr",
            "Assignment     = name: Expr, value: Expr",
            "IncDec         = operator: Token, expr: Expr",
            "Return         = keyword: Token, expr: Expr?",
            "Break          = keyword: Token",
            "Continue       = keyword: Token",
            "Block          = statements: List<Stmt>",
            "If             = condition: Expr, block: Block",
            "VariableDecl   = value: Expr, assignable: Boolean"
    ))

    defineAst("src/main/kotlin/com/github/crispyteam/parse", "Expr", listOf(
            "Binary     = left: Expr, operator: Token, right: Expr",
            "Unary      = operator: Token, expr: Expr",
            "Lambda     = parameters: List<Token>, body: Stmt",
            "Call       = callee: Expr, arguments: List<Expr>, paren: Token",
            "Get        = obj: Expr, key: Expr, brace: Token"
    ))
}

fun defineAst(outputDir: String, baseName: String, types: List<String>) {
    val path = "$outputDir/$baseName.kt"
    val writer = PrintWriter(path, "UTF-8")

    with(writer) {
        val typeNames = ArrayList<String>()

        print("""
       package com.github.crispyteam.parse

       import com.github.crispyteam.tokenize.Token


       """.trimIndent())

        println("abstract class $baseName {")

        println("    abstract fun <T> accept(visitor: Visitor<T>): T\n")

        for (type in types) {
            val split = type.split("=")
            val typeName = split[0].trim()
            val attributes = split[1].split(",").joinToString { "val $it" }
            typeNames += typeName

            println("    data class $typeName($attributes): $baseName() {")
            println("         override fun <T> accept(visitor: Visitor<T>): T {")
            println("              return visitor.visit$typeName(this)")
            println("         }")
            println("     }\n")
        }

        println("    interface Visitor<T> {")
        for (typeName in typeNames) {
            println("        fun visit$typeName(${typeName.toLowerCase()}$baseName: $typeName): T")
        }
        println("    }")

        println("}")
        close()
    }
}