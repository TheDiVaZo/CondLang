package org.thedivazo.dicesystem.parserexpression.lexer;

import lombok.NoArgsConstructor;
import org.apache.commons.collections4.map.LinkedMap;
import org.thedivazo.dicesystem.parserexpression.exception.FanoConditionException;
import org.thedivazo.dicesystem.parserexpression.exception.SyntaxException;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Лексер, который подвергает код лексическому анализу, и по завершению выдает список токенов.
 * @author TheDiVaZo
 * @version 1.2
 *
 */
@NoArgsConstructor
public class Lexer {
    /**
     * Хранит RegEx'ы и типы токенов, которые будут строиться по этим RegEx'ам.
     */
    protected Map<String, TokenType> tokenTypeMap = new LinkedMap<>();

    /**
     * @param regEx regEx, по которому будет присваиваться тип токена
     * @param tokenType Тип токена
     */
    public void putOperator(String regEx, TokenType tokenType) {
        tokenTypeMap.put(regEx, tokenType);
    }

    /**
     * Удаляет RegEx и токен по нему
     * @param sign regEx, по которому будет удаляться тип токена
     * @return тип удаленного токена
     */
    public TokenType removeOperator(String sign) {
       return tokenTypeMap.remove(sign);
    }


    /**
     * Метод анализирует код и строит токены по нему.
     * @param code исходный код. Пример: cond1 || cond2 !(cond3 && cond4)
     * @return Возвращает массив с токенами.
     * @throws SyntaxException если в коде присутствуют синтаксические ошибки, то будет вызвано это исключение;
     */
    public List<Token> analyze(String code) throws SyntaxException, FanoConditionException {
        List<Token> result = new ArrayList<>();
        StringBuilder sliceCode = new StringBuilder(code);
        int position = 0;
        TokenType requireNextToken = null;
        while(true) {
            if(sliceCode.isEmpty()) break;
            String token = null;
            TokenType tokenType = null;
            TokenType finalRequireNextToken = requireNextToken;
            Set<Map.Entry<String,TokenType>> entryTokenMap = Objects.isNull(requireNextToken) ? tokenTypeMap.entrySet() : tokenTypeMap.entrySet().stream().filter(entry->entry.getValue().equals(finalRequireNextToken)).collect(Collectors.toSet());
            for (Map.Entry<String,TokenType> entryToken : entryTokenMap) {
                String regEx = entryToken.getKey();
                Matcher matcher = Pattern.compile("^" + regEx).matcher(sliceCode);
                if (matcher.find()) {
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
