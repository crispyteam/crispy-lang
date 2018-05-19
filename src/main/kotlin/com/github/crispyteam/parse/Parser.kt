package com.github.crispyteam.parse

import com.github.crispyteam.tokenize.Lexer
import com.github.crispyteam.tokenize.Token
import com.github.crispyteam.tokenize.TokenType
import com.github.crispyteam.tokenize.TokenType.*

class Parser {
    class ParseError(token: Token, msg: String) :
            RuntimeException("[Error line: ${token.line}]: $msg")

    private lateinit var tokens: List<Token>
    private var position = 0

    fun parse(lexer: Lexer): List<Stmt> {
        tokens = lexer.lex()
        val statements = ArrayList<Stmt>()

        while (!atEnd()) {
            statements += stmt()
        }

        return statements
    }

    private fun peek(): Token =
            tokens[position]

    private fun consume(type: TokenType, msg: String): Token {
        if (check(type)) {
            advance()
            return previous()
        }
        throw ParseError(peek(), msg)
    }

    private fun atEnd(): Boolean =
            check(EOF)

    private fun previous(): Token =
            tokens[position - 1]

    private fun advance(): Token {
        if (!atEnd()) ++position
        return previous()
    }

    private fun match(vararg types: TokenType): Boolean =
            types.any { match(it) }

    private fun match(type: TokenType): Boolean {
        if (check(type)) {
            advance()
            return true
        }
        return false
    }

    private fun check(type: TokenType): Boolean =
            peek().type == type

    private fun stmt(): Stmt =
            when {
            // flow statements
                match(BREAK) -> breakStmt()
                match(CONTINUE) -> continueStmt()
                match(RETURN) -> returnStmt()
            // compound statements
                match(IF) -> ifStmt()
                match(WHILE) -> whileStmt()
                match(FOR) -> forStmt()
                check(OPEN_BRACE) -> block()
                else -> simpleStmt()
            }

    private fun simpleStmt(): Stmt =
            when {
                match(VAL) -> valDecl()
                match(VAR) -> varDecl()
                else -> exprStmt()
            }

    private fun exprStmt(): Stmt {
        val firstExpr = expr()

        return when {
            match(SEMICOLON) -> Stmt.Expression(expr = firstExpr)
            match(EQUALS) -> Stmt.Assignment(firstExpr, expr())
            match(PLUS_PLUS) || match(MINUS_MINUS) -> Stmt.IncDec(previous(), firstExpr)
            else -> throw ParseError(peek(), "Unexpected Token")
        }
    }

    private fun returnStmt(): Stmt.Return =
            Stmt.Return(previous(), expr())

    private fun continueStmt(): Stmt.Continue =
            Stmt.Continue(previous())

    private fun breakStmt(): Stmt.Break =
            Stmt.Break(previous())

    private fun block(): Stmt.Block {
        consume(OPEN_BRACE, "Expected '{' at beginning of block")
        val list = ArrayList<Stmt>()

        while (!match(CLOSE_BRACE)) {
            list += stmt()
            consume(SEMICOLON, "Expected ';' after statement")
        }

        return Stmt.Block(list)
    }

    private fun lambda(): Expr.Lambda {
        val params = paramList()

        val body = if (match(MINUS_GREATER)) {
            when {
                match(IF) -> ifStmt()
                match(WHILE) -> whileStmt()
                match(FOR) -> forStmt()
                check(OPEN_BRACE) -> block()
                else -> exprStmt()
            }
        } else {
            block()
        }

        return Expr.Lambda(params, body)
    }

    private fun paramList(): List<Token> {
        consume(OPEN_PAREN, "Expected '(' at beginning of function")
        val params = ArrayList<Token>()

        if (!check(CLOSE_PAREN)) {
            do {
                params += consume(IDENTIFIER, "Expected parameter name")
            } while (match(COMMA))
        }

        consume(CLOSE_PAREN, "Expected ')' after parameters")
        return params;
    }

    private fun forStmt(): Stmt {

    }

    private fun whileStmt(): Stmt {

    }

    private fun ifStmt(): Stmt.If {
        val condition = expr()
        val block = block()

        return Stmt.If(condition, block)
    }

    private fun valDecl(): Stmt.VariableDecl {
        val value = expr()
        return Stmt.VariableDecl(value, assignable = false)
    }

    private fun varDecl(): Stmt {
        val value = expr()
        return Stmt.VariableDecl(value, assignable = true)
    }

    private fun expr(): Expr =
            when {
                match(FUN) -> lambda()
                else -> logicOr()
            }

    private fun logicOr(): Expr {
        var result = logicAnd()

        while (match(OR)) {
            result = Expr.Binary(result, previous(), logicAnd())
        }

        return result
    }

    private fun logicAnd(): Expr {
        var result = equality()

        while (match(AND)) {
            result = Expr.Binary(result, previous(), equality())
        }

        return result
    }

    private fun equality(): Expr {
        var result = comparison()

        while (match(EQUALS_EQUALS, BANG_EQUALS)) {
            result = Expr.Binary(result, previous(), comparison())
        }

        return result
    }

    private fun comparison(): Expr {
        var result = arithExpr()

        while (match(SMALLER, SMALLER_EQUALS, GREATER, GREATER_EQUALS)) {
            result = Expr.Binary(result, previous(), arithExpr())
        }

        return result
    }

    private fun arithExpr(): Expr {
        var result = term()

        while (match(PLUS, MINUS)) {
            result = Expr.Binary(result, previous(), term())
        }

        return result
    }

    private fun term(): Expr {
        var result = factor()

        while (match(STAR, SLASH)) {
            result = Expr.Binary(result, previous(), factor())
        }

        return result
    }

    private fun factor(): Expr {
        if (match(MINUS, BANG)) {
            return Expr.Unary(previous(), factor())
        }

        return primaryExpr()
    }

    private fun primaryExpr(): Expr {
        var primary = primary()


    }

    private fun call(): Expr? {

    }

    private fun primary(): Expr {

    }
}
