package com.github.crispyteam.tools

import java.io.PrintWriter

fun main(args: Array<String>) {

}

fun defineAst(outputDir: String, baseName: String, types: List<String>) {
    val path = "$outputDir/$baseName.kt"
    val writer = PrintWriter(path, "UTF-8")

    with(writer) {
        val typeNames = ArrayList<String>()

        print("""
       package com.provinzial.klox.parsing

       import com.provinzial.klox.lexing.Token


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