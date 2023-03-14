package org.thedivazo.dicesystem.parserexpression.lexer;

public enum TokenType {
    EOF,
    CONDITION,

    OPERATOR,

    FUNCTION,
    COMPOUND_START,
    COMPOUND_END,
    SPACE,
    DELIMITER,
    METHOD,
    METHOD_REFERENCE() {
        @Override
        public TokenType requireNextToken() {
            return TokenType.METHOD;
        }
    },
    START_VARIABLE_SYMBOL() {
        @Override
        public TokenType requireNextToken() {
            return TokenType.CONDITION;
        }
    };



    public TokenType requireNextToken(){
        return null;
    };
}
