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

    private fun error(token: Token, message: String): ParseError {
        return ParseError(token, message)
    }

    private fun peek(): Token =
            tokens[position]

    private fun consume(type: TokenType, msg: String): Token {
        if (check(type)) {
            advance()
            return previous()
        }
        throw error(peek(), msg)
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
        val expr = expr()
        consume(SEMICOLON, "Expected ';' after statement")
        return Stmt.Expression(expr)
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
        }

        return Stmt.Block(list)
    }

    private fun lambda(): Expr.Lambda {
        val params = paramList()

        consume(MINUS_GREATER, "Expected '->' after parameters")
        val body = when {
            check(OPEN_BRACE) -> block()
            else -> Stmt.Expression(expr())
        }

        return Expr.Lambda(params, body)
    }

    private fun paramList(): List<Token> {
        val params = ArrayList<Token>()

        if (!check(MINUS_GREATER)) {
            do {
                params += consume(IDENTIFIER, "Expected parameter name")
            } while (match(COMMA))
        }

        return params
    }

    private fun forStmt(): Stmt {
        val initializer: Stmt? = when {
            match(SEMICOLON) -> null
            match(VAR) -> varDecl()
            else -> exprStmt()
        }

        var condition: Expr? = when {
            check(SEMICOLON) -> null
            else -> expr()
        }
        consume(SEMICOLON, "Expected ';' after loop condition")

        val increment: Expr? = when {
            check(OPEN_BRACE) -> null
            else -> expr()
        }

        var body = block() as Stmt

        // add increment after block
        if (increment != null) {
            body = Stmt.Block(listOf(body, Stmt.Expression(increment)))
        }

        condition = condition ?: Expr.Literal(true)
        body = Stmt.While(condition, body)

        if (initializer != null) {
            body = Stmt.Block(listOf(initializer, body))
        }

        return body
    }

    private fun whileStmt(): Stmt.While {
        val condition = expr()
        val block = block()

        return Stmt.While(condition, block)
    }

    private fun ifStmt(): Stmt.If {
        val condition = expr()
        val block = block()

        val elseBranch = if (match(ELSE)) {
            if (check(OPEN_BRACE)) {
                block()
            } else {
                consume(IF, "Expected if or block after else")
                ifStmt()
            }
        } else {
            null
        }

        return Stmt.If(condition, block, elseBranch)
    }

    private fun valDecl(): Stmt.ValDecl {
        val name = consume(IDENTIFIER, "Expected variable name after 'val'")
        consume(EQUALS, "Expected initialization of value")
        val value = expr()

        consume(SEMICOLON, "Expected ';' after val initialization")
        return Stmt.ValDecl(name, value)
    }

    private fun varDecl(): Stmt.VarDecl {
        val name = consume(IDENTIFIER, "Expected variable name after 'var'")
        val value = if (match(EQUALS)) {
            expr()
        } else {
            null
        }
        consume(SEMICOLON, "Expected ';' after var initialization")
        return Stmt.VarDecl(name, value)
    }

    private fun expr(): Expr =
            when {
                match(FUN) -> lambda()
                else -> assignment()
            }

    private fun assignment(): Expr {
        val result = logicOr()

        return when {
            match(EQUALS) -> Expr.Assignment(result, assignment())
            match(PLUS_PLUS) -> Expr.Increment(result)
            match(MINUS_MINUS) -> Expr.Decrement(result)
            else -> result
        }
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

        while (match(OPEN_PAREN, OPEN_BRACKET, DOT)) {
            primary = when (previous().type) {
                OPEN_PAREN -> {
                    val paren = previous()
                    val args: List<Expr> = if (match(CLOSE_PAREN))
                        emptyList()
                    else
                        argList()
                    Expr.Call(primary, args, paren)
                }
                OPEN_BRACKET -> {
                    val bracket = previous()
                    val expr = expr()
                    consume(CLOSE_BRACKET, "Expected ']' after get expression")
                    Expr.Get(primary, expr, bracket)
                }
                DOT -> {
                    val dot = previous()
                    val ident = consume(IDENTIFIER, "Expected identifer after '.'")
                    Expr.Get(primary, Expr.Literal(ident.literal), dot)
                }
                else -> throw error(previous(), "Unexpected Token")
            }
        }

        return primary
    }

    private fun argList(): List<Expr> {
        val args = ArrayList<Expr>()

        do {
            args += expr()
        } while (match(COMMA))

        consume(CLOSE_PAREN, "Expected ')' after arguments")

        return args
    }

    private fun primary(): Expr {
        return when {
            match(TRUE) -> Expr.Literal(true)
            match(FALSE) -> Expr.Literal(false)
            match(NIL) -> Expr.Literal(null)
            match(NUMBER) || match(STRING) -> Expr.Literal(previous().literal)

            match(IDENTIFIER) -> Expr.Variable(previous())
            match(OPEN_PAREN) -> {
                val expr = expr()
                consume(CLOSE_PAREN, "Expected '(' after expression")
                Expr.Grouping(expr)
            }

            match(OPEN_BRACE) -> if (!match(CLOSE_BRACE)) {
                Expr.Dictionary(dictItems())
            } else {
                Expr.Dictionary(emptyList())
            }

            match(OPEN_BRACKET) -> if (!match(CLOSE_BRACKET)) {
                Expr.CrispyList(listItems())
            } else {
                Expr.CrispyList(emptyList())
            }

            else -> throw error(peek(), "Expected expression")
        }
    }

    private fun listItems(): List<Expr> {
        val items = ArrayList<Expr>()

        do {
            items += expr()
        } while (match(COMMA))

        consume(CLOSE_BRACKET, "Expected ']' after arguments")

        return items
    }

    private fun dictItems(): List<Pair<Expr, Expr>> {
        val items = ArrayList<Pair<Expr, Expr>>()

        do {
            val key = expr()
            consume(COLON, "Expected ':' between key and value in dictionary")
            val value = expr()
            items += key to value
        } while (match(COMMA))

        consume(CLOSE_BRACE, "Expected '}' after dictionary literal")

        return items
    }
}