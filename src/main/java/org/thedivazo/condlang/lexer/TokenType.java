package org.thedivazo.condlang.lexer;

public enum TokenType {
    EOF,
    CONDITION,

    OPERATOR,

    FUNCTION,
    COMPOUND_START,
    COMPOUND_END,
    SPACE,
    DELIMITER,
    METHOD {
        @Override
        public boolean isIndependentToken() {
            return false;
        }
    },
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
    }
    public boolean isIndependentToken(){
        return true;
    }
}
