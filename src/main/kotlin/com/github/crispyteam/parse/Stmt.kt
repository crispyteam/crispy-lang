package com.github.crispyteam.parse

import com.github.crispyteam.tokenize.Token

abstract class Stmt {
    abstract fun <T> accept(visitor: Visitor<T>): T

    data class Expression(val  expr: Expr): Stmt() {
         override fun <T> accept(visitor: Visitor<T>): T {
              return visitor.visitExpression(this)
         }
     }

    data class Assignment(val  name: Expr, val  value: Expr): Stmt() {
         override fun <T> accept(visitor: Visitor<T>): T {
              return visitor.visitAssignment(this)
         }
     }

    data class IncDec(val  operator: Token, val  expr: Expr): Stmt() {
         override fun <T> accept(visitor: Visitor<T>): T {
              return visitor.visitIncDec(this)
         }
     }

    data class Return(val  keyword: Token, val  expr: Expr?): Stmt() {
         override fun <T> accept(visitor: Visitor<T>): T {
              return visitor.visitReturn(this)
         }
     }

    data class Break(val  keyword: Token): Stmt() {
         override fun <T> accept(visitor: Visitor<T>): T {
              return visitor.visitBreak(this)
         }
     }

    data class Continue(val  keyword: Token): Stmt() {
         override fun <T> accept(visitor: Visitor<T>): T {
              return visitor.visitContinue(this)
         }
     }

    data class Block(val  statements: List<Stmt>): Stmt() {
         override fun <T> accept(visitor: Visitor<T>): T {
              return visitor.visitBlock(this)
         }
     }

    data class If(val  condition: Expr, val  block: Block): Stmt() {
         override fun <T> accept(visitor: Visitor<T>): T {
              return visitor.visitIf(this)
         }
     }

    data class VariableDecl(val  value: Expr, val  assignable: Boolean): Stmt() {
         override fun <T> accept(visitor: Visitor<T>): T {
              return visitor.visitVariableDecl(this)
         }
     }

    interface Visitor<T> {
        fun visitExpression(expressionStmt: Expression): T
        fun visitAssignment(assignmentStmt: Assignment): T
        fun visitIncDec(incdecStmt: IncDec): T
        fun visitReturn(returnStmt: Return): T
        fun visitBreak(breakStmt: Break): T
        fun visitContinue(continueStmt: Continue): T
        fun visitBlock(blockStmt: Block): T
        fun visitIf(ifStmt: If): T
        fun visitVariableDecl(variabledeclStmt: VariableDecl): T
    }
}
