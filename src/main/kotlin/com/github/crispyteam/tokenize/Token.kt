package com.github.crispyteam.tokenize

enum class TokenType {
    /* Single character */
    PLUS,
    MINUS, STAR, SLASH, PERCENT, BANG,
    EQUALS, SMALLER, GREATER, OPEN_BRACKET, CLOSE_BRACKET, DOT,
    SEMICOLON, OPEN_PAREN, CLOSE_PAREN, OPEN_BRACE,
    CLOSE_BRACE, COMMA, COLON,

    /* Two character */
    EQUALS_EQUALS,
    BANG_EQUALS,
    SMALLER_EQUALS, GREATER_EQUALS,

    /* Literals */
    STRINGS,
    NUMBER, IDENTIFIER,

    /* Keywords */
    FUN,
    VAL, VAR, CONTINUE, BREAK, IF, ELSE, FOR, WHILE, IMPORT, IN, RETURN, TRUE, FALSE, NIL, AND, OR,

    EOF
}