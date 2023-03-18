package org.thedivazo.condlang.parser;

import org.thedivazo.condlang.lexer.Token;
import org.thedivazo.condlang.lexer.TokenType;

import java.util.List;
import java.util.Objects;

public class TokenBuffer {
    protected final List<Token> tokenList;

    @Override
    public String toString() {
        return String.join("",tokenList.stream().map(Token::toString).toList());
    }

    public String tokensToCode() {
        StringBuilder code = new StringBuilder();
        Token prevToken = null;
        for (Token token : tokenList) {
            if(token.getLexemeType().equals(TokenType.EOF)) break;
            int numberSpace = Objects.isNull(prevToken) ? token.getPosition(): token.getPosition()-(prevToken.getPosition()+prevToken.getSign().length());
            code.append(" ".repeat(Math.max(numberSpace, 0))).append(token.getSign());
            prevToken=token;
        }
        return code.toString();
    }

    public TokenBuffer(List<Token> tokenList) {
        this.tokenList = tokenList.stream().filter(token -> !token.getLexemeType().equals(TokenType.SPACE)).toList();
    }

    protected int currentIndex = 0;

    public Token next() {
        return tokenList.get(currentIndex++);
    }

    public void prev() {
        currentIndex--;
    }

    public boolean hasNext() {
        return tokenList.size() > currentIndex;
    }

    public Token current() {
        return tokenList.get(currentIndex);
    }
}
