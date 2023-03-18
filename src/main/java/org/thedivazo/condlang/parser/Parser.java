package org.thedivazo.condlang.parser;

import lombok.*;
import org.apache.commons.collections4.list.SetUniqueList;
import org.intellij.lang.annotations.RegExp;
import org.thedivazo.condlang.exception.CompileException;
import org.thedivazo.condlang.exception.SyntaxException;
import org.thedivazo.condlang.lexer.Lexer;
import org.thedivazo.condlang.lexer.Token;
import org.thedivazo.condlang.lexer.TokenType;
import org.thedivazo.condlang.parser.AST.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Данный класс предназначен для парсинга списка токенов ({@link Lexer}) в дерево узлов (AST).
 *
 * @author TheDiVaZo
 * @version 2.1
 */
@RequiredArgsConstructor
public class Parser {

    /*
         TERNARY_OPERATOR: OPERATOR_-1 ("operand_1" TERNARY_OPERATOR "operand_2" TERNARY_OPERATOR)?
         BINARY_OPERATOR: OPERATOR_-1 "operand" BINARY_OPERATOR
         UNARY_OPERATOR: "operand"? OPERATOR_-1
         FUNCTION: "function_name"\((expr)?(,expr)*\)
         METHOD: CONDITION"method_reference""method_name"\((expr)?(,expr)*\)

         NUMBERS: [0-9]+(\\.[0-9]+)?
         CONDITION: [a-zA-Z0-9]+ |  (EXPRESS)
     */

    @AllArgsConstructor
    @Getter
    @EqualsAndHashCode
    public static class OperatorData {
        private String signOperator;
        private OperatorType operatorType;

        @Override
        public String toString() {
            return signOperator+operatorType;
        }
    }


    private final List<Set<OperatorData>> listOfPriorityOperator = SetUniqueList.setUniqueList(new ArrayList<>());

    /**
     * Метод, позволяющий добавить токен(ы) оператора(ов) в список приоритета.
     * Чем позже был добавлен оператор(ы) в список, тем более он(и) приоритетен(ны).
     * @param operatorsData Массив с неповторяющимися множествами операторов, которые вы хотите добавить в список приоритета
     * @return возвращает состояние добавления
     */
    public boolean addOperator(OperatorData... operatorsData) {
        Set<OperatorData> operatorDataSet = new LinkedHashSet<>();
        for (OperatorData operatorData : operatorsData) {
            operatorDataSet.add(operatorData);
        }
        return listOfPriorityOperator.add(operatorDataSet);
    }

    /**
     * @param tokenList Массив с токенами, который нужно преобразовать в AST дерево
     * @return Возвращает головной узел
     */
    public Node parsing(List<Token> tokenList) throws CompileException {
        return expr(new TokenBuffer(tokenList));
    }

    protected Node expr(TokenBuffer tokenBuffer) throws CompileException {
        if(tokenBuffer.tokenList.isEmpty()) throw new SyntaxException("The expression cannot be empty",0,tokenBuffer.tokensToCode());
        if(tokenBuffer.next().getLexemeType().equals(TokenType.EOF)) throw new SyntaxException("The expression cannot be empty",tokenBuffer.current().getPosition(),tokenBuffer.tokensToCode());
        tokenBuffer.prev();
        return operator(tokenBuffer, listOfPriorityOperator.size()-1);
    }

    public Set<OperatorData> getOperatorForIndex(int indexPriority) {
        return listOfPriorityOperator.get(indexPriority);
    }

    protected Node ternaryOperator(TokenBuffer tokenBuffer, Set<OperatorData> operatorData, int indexPriority) throws CompileException {
        OperatorData operatorDataOne = operatorData.stream().toList().get(0);
        OperatorData operatorDataTwo = operatorData.stream().toList().get(1);
        Node argumentOneNode = operator(tokenBuffer, indexPriority-1);
        Token operatorOne = tokenBuffer.next();
        if(!operatorOne.getSign().equals(operatorDataOne.getSignOperator()) || !operatorOne.getLexemeType().equals(TokenType.OPERATOR)){
            tokenBuffer.prev();
            return argumentOneNode;
        }
        Node argumentTwoNode = operator(tokenBuffer, indexPriority);
        Token operatorTwo = tokenBuffer.next();
        if(!operatorTwo.getLexemeType().equals(TokenType.OPERATOR) && operatorDataTwo.getOperatorType().equals(OperatorType.TERNARY_2)) throw new SyntaxException("Ternary operator expected", operatorTwo.getPosition(), tokenBuffer.tokensToCode());
        Node argumentThreeNode = operator(tokenBuffer, indexPriority);
        TernaryOperatorNode ternaryOperatorNode = new TernaryOperatorNode(operatorOne.getSign()+operatorTwo.getSign());
        ternaryOperatorNode.setNodes(argumentOneNode, argumentTwoNode, argumentThreeNode);
        return ternaryOperatorNode;
    }

    protected Node binaryOperator(TokenBuffer tokenBuffer, Set<OperatorData> operatorsData, int indexPriority) throws CompileException {
        Node prevNode = operator(tokenBuffer, indexPriority-1);
        while (tokenBuffer.hasNext()) {
            Token token = tokenBuffer.next();
            if(operatorsData.stream().anyMatch(operatorData -> token.getSign().equals(operatorData.getSignOperator())) && token.lexemeType().equals(TokenType.OPERATOR)) {
                Node firstOperatorArgument = prevNode;
                Node secondOperatorArgument = operator(tokenBuffer, indexPriority-1);
                BinaryOperatorNode binaryOperatorNode = new BinaryOperatorNode(token.getSign());
                binaryOperatorNode.setNodes(firstOperatorArgument,secondOperatorArgument);
                prevNode = binaryOperatorNode;
            }
            else {
                tokenBuffer.prev();
                break;
            }
        }
        return prevNode;
    }

    protected Node unaryOperator(TokenBuffer tokenBuffer, Set<OperatorData> operatorsData, int indexPriority) throws CompileException {
        Token token = tokenBuffer.next();
        Node argument = null;
        if(operatorsData.stream().anyMatch(operatorData -> operatorData.getSignOperator().equals(token.getSign())) && token.getLexemeType().equals(TokenType.OPERATOR)) {
            UnaryOperatorNode unaryOperatorNode = new UnaryOperatorNode(token.getSign());
            argument = operator(tokenBuffer, indexPriority);
            unaryOperatorNode.setNodes(argument);
            return unaryOperatorNode;
        }
        tokenBuffer.prev();
        argument = operator(tokenBuffer, indexPriority-1);
        return argument;
    }

    protected Node operator(TokenBuffer tokenBuffer,int indexPriority) throws CompileException {
        if(indexPriority < 0) return method(tokenBuffer);
        Set<OperatorData> operatorsData = getOperatorForIndex(indexPriority);
        if(operatorsData.size() == 2) {
            List<OperatorData> operatorDataList = operatorsData.stream().toList();
            if(operatorDataList.get(0).getOperatorType().equals(OperatorType.TERNARY_1) && operatorDataList.get(1).getOperatorType().equals(OperatorType.TERNARY_2))
                return ternaryOperator(tokenBuffer, operatorsData, indexPriority);
        }
        if(operatorsData.stream().allMatch(operatorData -> operatorData.getOperatorType().equals(OperatorType.BINARY))) return binaryOperator(tokenBuffer, operatorsData, indexPriority);
        else if(operatorsData.stream().allMatch(operatorData -> operatorData.getOperatorType().equals(OperatorType.UNARY))) return unaryOperator(tokenBuffer, operatorsData, indexPriority);
        else {
            System.out.println(operatorsData.stream().toList().get(0));
            System.out.println(operatorsData.stream().toList().get(1));
            throw new CompileException("Operators of different kinds must have different precedence");
        }
    }

    protected Node factor(TokenBuffer tokenBuffer) throws CompileException {
        Token currentToken = tokenBuffer.next();
        switch (currentToken.getLexemeType()) {

            case FUNCTION -> {
                tokenBuffer.prev();
                return function(tokenBuffer);
            }

            case CONDITION -> {
                return new ConditionNode(currentToken.getSign());
            }

            case START_VARIABLE_SYMBOL -> {
                Token variableToken = tokenBuffer.next();
                if(variableToken.lexemeType() != TokenType.CONDITION) throw new SyntaxException("Local argument expected", variableToken.getPosition(), tokenBuffer.tokensToCode());
                return new ConditionNode(variableToken.getSign());
            }

            case COMPOUND_START -> {
                Node compoundNode = expr(tokenBuffer);
                Token nextToken = tokenBuffer.next();
                if(!nextToken.getLexemeType().equals(TokenType.COMPOUND_END)) throw new SyntaxException("Missing closing compound", nextToken.getPosition(), tokenBuffer.tokensToCode());
                return compoundNode;
            }
            default -> throw new SyntaxException("Unknown condition", currentToken.getPosition(), tokenBuffer.tokensToCode());

        }
    }

    protected Node function(TokenBuffer tokenBuffer) throws CompileException {
        Token currentToken = tokenBuffer.next();
        if(!currentToken.lexemeType().equals(TokenType.FUNCTION)) return method(tokenBuffer);
        if(!tokenBuffer.next().lexemeType().equals(TokenType.COMPOUND_START)) throw new SyntaxException("Compound start expected", currentToken.getPosition(), tokenBuffer.tokensToCode());

        List<Node> expressionList = new ArrayList<>();
        if(!tokenBuffer.next().lexemeType().equals(TokenType.COMPOUND_END)) {
            tokenBuffer.prev();
            while (tokenBuffer.hasNext()) {
                Node expression = expr(tokenBuffer);
                expressionList.add(expression);
                if(tokenBuffer.current().lexemeType().equals(TokenType.COMPOUND_END)) {
                    tokenBuffer.next();
                    break;
                };
                if(tokenBuffer.current().lexemeType().equals(TokenType.DELIMITER)) {
                    tokenBuffer.next();
                    continue;
                }
                else throw new SyntaxException("Compound end expected", tokenBuffer.current().getPosition(),tokenBuffer.tokensToCode());
            }
        }
        FunctionOperatorNode functionOperatorNode = new FunctionOperatorNode(currentToken.getSign());
        functionOperatorNode.setNodes(expressionList.toArray(new Node[]{}));
        return functionOperatorNode;
    }

    protected Node method(TokenBuffer tokenBuffer) throws CompileException {
        Node conditionNode = factor(tokenBuffer);
        while (tokenBuffer.hasNext()) {
            Token token = tokenBuffer.next();
            if (token.lexemeType() != TokenType.METHOD_REFERENCE) {
                tokenBuffer.prev();
                return conditionNode;
            }
            Token methodToken = tokenBuffer.next();
            if(methodToken.lexemeType() != TokenType.METHOD) throw new SyntaxException("The \""+methodToken.getSign()+"\" operator must be followed by a method", methodToken.getPosition(),tokenBuffer.tokensToCode());
            if(!tokenBuffer.next().lexemeType().equals(TokenType.COMPOUND_START)) throw new SyntaxException("Compound start expected", tokenBuffer.current().getPosition(), tokenBuffer.tokensToCode());
            List<Node> expressionList = new ArrayList<>();
            if(!tokenBuffer.next().lexemeType().equals(TokenType.COMPOUND_END)) {
                tokenBuffer.prev();
                while (tokenBuffer.hasNext()) {
                    Node expression = expr(tokenBuffer);
                    expressionList.add(expression);
                    if(tokenBuffer.current().lexemeType().equals(TokenType.COMPOUND_END)) {
                        tokenBuffer.next();
                        break;
                    };
                    if(tokenBuffer.current().lexemeType().equals(TokenType.DELIMITER)) {
                        tokenBuffer.next();
                        continue;
                    }
                    else throw new SyntaxException("Compound end expected", tokenBuffer.current().getPosition(),tokenBuffer.tokensToCode());
                }
            }
            MethodOperatorNode methodOperatorNode = new MethodOperatorNode(methodToken.getSign());
            methodOperatorNode.setNodes(expressionList.toArray(new Node[]{}));
            methodOperatorNode.setContext(conditionNode);
            conditionNode = methodOperatorNode;
        }
        return conditionNode;
    }

    protected String tokensToCode(List<Token> tokenList) {
        return String.join("",tokenList.stream().map(Token::getSign).toList());
    }
}
