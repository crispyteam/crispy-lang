package com.github.crispyteam.parse

import com.github.crispyteam.tokenize.Token

abstract class Expr {
    abstract fun <T> accept(visitor: Visitor<T>): T

    data class Binary(val  left: Expr, val  operator: Token, val  right: Expr): Expr() {
         override fun <T> accept(visitor: Visitor<T>): T {
              return visitor.visitBinary(this)
         }
     }

    data class Unary(val  operator: Token, val  expr: Expr): Expr() {
         override fun <T> accept(visitor: Visitor<T>): T {
              return visitor.visitUnary(this)
         }
     }

    data class Lambda(val  parameters: List<Token>, val  body: Stmt): Expr() {
         override fun <T> accept(visitor: Visitor<T>): T {
              return visitor.visitLambda(this)
         }
     }

    data class Call(val  callee: Expr, val  arguments: List<Expr>, val  paren: Token): Expr() {
         override fun <T> accept(visitor: Visitor<T>): T {
              return visitor.visitCall(this)
         }
     }

    data class Get(val  obj: Expr, val  key: Expr, val  brace: Token): Expr() {
         override fun <T> accept(visitor: Visitor<T>): T {
              return visitor.visitGet(this)
         }
     }

    interface Visitor<T> {
        fun visitBinary(binaryExpr: Binary): T
        fun visitUnary(unaryExpr: Unary): T
        fun visitLambda(lambdaExpr: Lambda): T
        fun visitCall(callExpr: Call): T
        fun visitGet(getExpr: Get): T
    }
}
