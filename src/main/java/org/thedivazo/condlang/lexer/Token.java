package org.thedivazo.dicesystem.parserexpression.lexer;

import lombok.Getter;

import java.util.Objects;


public record Token(@Getter TokenType lexemeType, @Getter String sign, int position) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Token token)) return false;
        return lexemeType() == token.lexemeType() && sign().equals(token.sign());
    }

    @Override
    public int hashCode() {
        return Objects.hash(lexemeType(), sign());
    }

    @Override
    public String toString() {
        return sign;
    }

    public int getPosition() {
        return position;
    }
}
