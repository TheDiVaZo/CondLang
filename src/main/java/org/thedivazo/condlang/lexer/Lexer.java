package org.thedivazo.condlang.lexer;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.map.LinkedMap;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.intellij.lang.annotations.RegExp;
import org.thedivazo.condlang.exception.FanoConditionException;
import org.thedivazo.condlang.exception.SyntaxException;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Лексер, который подвергает код лексическому анализу, и по завершению выдает список токенов.
 * @author TheDiVaZo
 * @version 1.3
 *
 */
@NoArgsConstructor
public class Lexer {
    /**
     * Хранит RegEx'ы и типы токенов, которые будут строиться по этим RegEx'ам.
     */
    protected Multimap<String, TokenType> tokenTypeMap = MultimapBuilder.hashKeys().arrayListValues().build();

    /**
     * @param regEx regEx, по которому будет присваиваться тип токена
     * @param tokenType Тип токена
     */

    public void putOperator(@RegExp String regEx, TokenType tokenType) {
        tokenTypeMap.put(regEx, tokenType);
    }

    /**
     * Удаляет RegEx и токен по нему
     * @param sign regEx, по которому будет удаляться тип токена
     * @return тип удаленного токена
     */
    public void removeOperator(@RegExp String sign) {
        tokenTypeMap.removeAll(sign);
    }

    public void removeOperator(@RegExp String sign, TokenType tokenType) {
        tokenTypeMap.remove(sign, tokenType);
    }


    /**
     * Метод анализирует код и строит токены по нему.
     * @param code исходный код. Пример: cond1 || cond2 !(cond3 && cond4)
     * @return Возвращает массив с токенами.
     * @throws SyntaxException если в коде присутствуют синтаксические ошибки, то будет вызвано это исключение;
     */
    public List<Token> analyze(String code) throws SyntaxException {
        List<Token> result = new ArrayList<>();
        StringBuilder sliceCode = new StringBuilder(code);
        int position = 0;
        TokenType requireNextToken = null;
        while(true) {
            if(sliceCode.isEmpty()) break;
            String token = null;
            TokenType tokenType = null;
            TokenType finalRequireNextToken = requireNextToken;
            Collection<Map.Entry<String,TokenType>> entryTokenMap = Objects.isNull(requireNextToken) ? tokenTypeMap.entries() : tokenTypeMap.entries().stream().filter(entry->entry.getValue().equals(finalRequireNextToken)).collect(Collectors.toSet());
            for (Map.Entry<String,TokenType> entryToken : entryTokenMap) {
                String regEx = entryToken.getKey();
                Matcher matcher = Pattern.compile("^" + regEx).matcher(sliceCode);
                if (matcher.find() && (entryToken.getValue().isIndependentToken() || entryToken.getValue().equals(finalRequireNextToken))) {
                    token = matcher.group();
                    tokenType = entryToken.getValue();
                    break;
                }
            }
            Optional<TokenType> optionalTokenType = Optional.ofNullable(tokenType);
            requireNextToken = optionalTokenType.isEmpty() ? null:optionalTokenType.get().requireNextToken();

            if(Objects.isNull(token) || Objects.isNull(tokenType)) {
                token = sliceCode.substring(0,1);
                throw new SyntaxException(String.format("Unknown token: %s",token), position, code);
            }
            result.add(new Token(tokenType, token, position));
            position += token.length();
            sliceCode.replace(0, token.length(), "");
        }
        result.add(new Token(TokenType.EOF,"", code.length()));
        return result;
    }



}
