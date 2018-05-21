package com.github.crispyteam.parse

import com.github.crispyteam.tokenize.Token

abstract class Stmt {
    abstract fun <T> accept(visitor: Visitor<T>): T

    data class Expression(val  expr: Expr): Stmt() {
         override fun <T> accept(visitor: Visitor<T>): T {
              return visitor.visitExpression(this)
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

    data class If(val  condition: Expr, val  thenBlock: Block, val  elseBlock: Stmt?): Stmt() {
         override fun <T> accept(visitor: Visitor<T>): T {
              return visitor.visitIf(this)
         }
     }

    data class ValDecl(val  name: Token, val  value: Expr): Stmt() {
         override fun <T> accept(visitor: Visitor<T>): T {
              return visitor.visitValDecl(this)
         }
     }

    data class VarDecl(val  name: Token, val  value: Expr?): Stmt() {
         override fun <T> accept(visitor: Visitor<T>): T {
              return visitor.visitVarDecl(this)
         }
     }

    data class While(val  condition: Expr, val  block: Stmt): Stmt() {
         override fun <T> accept(visitor: Visitor<T>): T {
              return visitor.visitWhile(this)
         }
     }

    data class Set(val  obj: Expr, val  key: Token, val  token: Token, val  value: Expr): Stmt() {
         override fun <T> accept(visitor: Visitor<T>): T {
              return visitor.visitSet(this)
         }
     }

    data class Assignment(val  name: Token, val  value: Expr): Stmt() {
         override fun <T> accept(visitor: Visitor<T>): T {
              return visitor.visitAssignment(this)
         }
     }

    data class Increment(val  variable: Token): Stmt() {
         override fun <T> accept(visitor: Visitor<T>): T {
              return visitor.visitIncrement(this)
         }
     }

    data class Decrement(val  variable: Token): Stmt() {
         override fun <T> accept(visitor: Visitor<T>): T {
              return visitor.visitDecrement(this)
         }
     }

    interface Visitor<T> {
        fun visitExpression(expressionStmt: Expression): T
        fun visitReturn(returnStmt: Return): T
        fun visitBreak(breakStmt: Break): T
        fun visitContinue(continueStmt: Continue): T
        fun visitBlock(blockStmt: Block): T
        fun visitIf(ifStmt: If): T
        fun visitValDecl(valdeclStmt: ValDecl): T
        fun visitVarDecl(vardeclStmt: VarDecl): T
        fun visitWhile(whileStmt: While): T
        fun visitSet(setStmt: Set): T
        fun visitAssignment(assignmentStmt: Assignment): T
        fun visitIncrement(incrementStmt: Increment): T
        fun visitDecrement(decrementStmt: Decrement): T
    }
}
