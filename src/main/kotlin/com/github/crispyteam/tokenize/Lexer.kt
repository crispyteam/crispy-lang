package com.github.crispyteam.tokenize

import com.github.crispyteam.reportError
import com.github.crispyteam.tokenize.TokenType.*

class Lexer(private val source: String) {
    private val tokens: MutableList<Token> = ArrayList()
    // Crispys reserved keywords
    private val keywords: Map<String, TokenType> = mapOf(
            "fun" to FUN,
            "val" to VAL,
            "var" to VAR,
            "continue" to CONTINUE,
            "break" to BREAK,
            "if" to IF,
            "else" to ELSE,
            "for" to FOR,
            "while" to WHILE,
            "import" to IMPORT, // unused, but reserved
            "in" to IN,  // unused, but reserved
            "return" to RETURN,
            "true" to TRUE,
            "false" to FALSE,
            "nil" to NIL,
            "and" to AND,
            "or" to OR
    )

    // The current position in the source code
    private var position = 0
    // The position at the start of the current Token
    private var start = 0
    // The current line in the source code
    private var line = 0

    /**
     * Performs lexical analysis on the source code and returns
     * it as a List of Tokens.
     *
     * @return Every Token in the code (+ EOF).
     */
    fun lex(): List<Token> {
        tokens.clear()
        position = 0
        line = 1

        while (!atEnd()) {
            start = position
            lexToken()
        }

        // manually add EOF Token
        tokens += Token(
                type = EOF,
                line = line,
                startPos = source.length - 1,
                endPos = source.length - 1,
                literal = null
        )

        return tokens
    }

    /**
     * Checks if the current position exceeds
     * the length of the source code.
     */
    private fun atEnd() =
            position >= source.length

    /**
     * Increments the position counter.
     *
     * @return The char at position, before incrementing or \0.
     */
    private fun advance() =
            if (!atEnd())
                source[position++]
            else
                0.toChar()

    /**
     * Adds a Token without a literal to the List.
     */
    private fun addToken(type: TokenType) =
            addToken(type, null)

    /**
     * Adds a Token with the supplied literal to the List.
     */
    private fun addToken(type: TokenType, literal: Any?) {
        tokens += Token(type, line, start, position, literal)
    }

    /**
     * Returns the source code as a List of lines.
     * Used for error messages.
     */
    private fun lines() = source.lines()

    /**
     * The current char.
     *
     * @return The char at the current position or \0.
     */
    private fun peek() =
            if (atEnd())
                0.toChar()
            else
                source[position]

    /**
     * The next char.
     *
     * @return The char at the next position or \0.
     */
    private fun peekNext() =
            if (position + 1 < source.length)
                source[position + 1]
            else
                0.toChar()

    /**
     * Reads an entire number from the source code and adds it to
     * the List of Tokens.
     */
    private fun number() {
        while (peek() in '0'..'9') advance()

        if (peek() == '.' && peekNext() in '0'..'9') advance()

        while (peek() in '0'..'9') advance()

        val text = source.substring(start, position)
        addToken(NUMBER, text.toDouble())
    }

    /**
     * Reads an entire string from the source code and adds it to
     * the List of Tokens.
     */
    private fun string() {
        while (!atEnd() && peek() != '"') {
            if (peek() == '\n') ++line
            advance()
        }

        if (atEnd()) {
            reportError(line, "Unterminated string")
            return
        }

        // consume Trailing '"'
        advance()

        val text = source.substring(start + 1, position - 1)
        addToken(STRING, text)
    }

    /**
     * Reads an entire identifier from the source code and adds it to
     * the List of Tokens.
     */
    private fun identifier() {
        while (peek().isLetterOrDigit() || peek() == '_') {
            advance()
        }

        val text = source.substring(start until position)
        val type = keywords[text]

        addToken(type ?: IDENTIFIER, text)
    }

    /**
     * Advances if the current char is the same as c.
     *
     * @return true, if c is the current char, false otherwise.
     */
    private fun match(c: Char): Boolean {
        if (peek() == c) {
            advance()
            return true
        }
        return false
    }

    /**
     * Skips until the next newline char.
     */
    private fun lineComment() {
        while (!atEnd() && peek() != '\n')
            advance()
    }

    /**
     * Skips until the end of the comment.
     */
    private fun multiLineComment() {
        while (!atEnd()) {
            val current = advance()
            if (current == '*' && match('/'))
                break
        }
    }

    /**
     * Adds the next Token to the List.
     */
    private fun lexToken() {
        val current = advance()

        when (current) {
            ' ', '\r', '\t' -> {
            }
            '\n' -> ++line
            '(' -> addToken(OPEN_PAREN)
            ')' -> addToken(CLOSE_PAREN)
            '{' -> addToken(OPEN_BRACE)
            '}' -> addToken(CLOSE_BRACE)
            '[' -> addToken(OPEN_BRACKET)
            ']' -> addToken(CLOSE_BRACKET)
            ':' -> addToken(COLON)
            '%' -> addToken(PERCENT)
            '+' -> {
                if (match('+'))
                    addToken(PLUS_PLUS)
                else
                    addToken(PLUS)
            }
            '-' -> when {
                match('>') -> addToken(MINUS_GREATER)
                match('-') -> addToken(MINUS_MINUS)
                else -> addToken(MINUS)
            }
            '*' -> addToken(STAR)
            '/' -> when {
                match('/') -> lineComment()
                match('*') -> multiLineComment()
                else -> addToken(SLASH)
            }
            '.' -> addToken(DOT)
            ';' -> addToken(SEMICOLON)
            ',' -> addToken(COMMA)
            '"' -> string()
            in '0'..'9' -> number()
            in 'a'..'z', in 'A'..'Z', '_' -> identifier()
            '!' -> {
                if (match('='))
                    addToken(BANG_EQUALS)
                else
                    addToken(BANG)
            }
            '>' -> {
                if (match('='))
                    addToken(GREATER_EQUALS)
                else
                    addToken(GREATER)
            }
            '<' -> {
                if (match('='))
                    addToken(SMALLER_EQUALS)
                else
                    addToken(SMALLER)
            }
            '=' -> {
                if (match('='))
                    addToken(EQUALS_EQUALS)
                else
                    addToken(EQUALS)
            }
            else -> reportError(line, "Unrecognized character: '$current'")
        }
    }
}