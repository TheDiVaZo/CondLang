package org.thedivazo.dicesystem.parserexpression.interpreter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.thedivazo.dicesystem.parserexpression.exception.InterpreterException;
import org.thedivazo.dicesystem.parserexpression.interpreter.wrapper.WrapperObject;
import org.thedivazo.dicesystem.parserexpression.parser.AST.*;
import org.thedivazo.dicesystem.utils.TernFunction;
import thedivazo.parserexpression.parser.AST.*;
import org.thedivazo.dicesystem.parserexpression.parser.Node;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;


/**
 * Класс, который исполняет команды узлов (AST)
 * @param <T> the type that the condition works with.
 * @param <R> the type that the true argument
 * @param <B> the type that returns the condition.
 * @author TheDiVaZo
 * @version 2.1
 */
public class Interpreter<T, R extends B, B> {

    @RequiredArgsConstructor
    class ConditionName {

        @Getter
        private final String regEx;

        @Getter
        private final BiFunction<T,String,B> condition;

    }

    protected List<ConditionName> listConditionNames = new ArrayList<>();

    @Getter
    @Setter
    protected Function<String, R> alternativeConditionParser;

    protected Map<String, TernFunction<Boolean,B,B,B>> listTernaryOperators = new HashMap<>();

    protected Map<String, BiFunction<B, B, R>> listBinaryOperators = new HashMap<>();

    protected Map<String, Function<B, R>> listUnaryOperators = new HashMap<>();

    protected Map<String, Function<List<B>,R>> listFunctionOperators = new HashMap<>();

    public void addTernaryOperator(String sign1, String sign2, TernFunction<Boolean, B,B,B> ternaryOperator) {
        listTernaryOperators.put(sign1+sign2, ternaryOperator);
    }

    public void addUnaryOperator(String sign, Function<B, R> unaryOperator) {
        listUnaryOperators.put(sign, unaryOperator);
    }

    public void addBinaryOperator(String sign, BiFunction<B,B,R> binaryOperator) {
        listBinaryOperators.put(sign, binaryOperator);
    }

    public void addFunctionOperator(String sign, Function<List<B>,R> functionOperator) {
        listFunctionOperators.put(sign, functionOperator);
    }

    public void addCondition(String regEx, BiFunction<T,String,B> condition) {
        listConditionNames.add(new ConditionName(regEx, condition));
    }

    public B execute(Node mainNode, T input) throws InterpreterException {
        return execute(mainNode, input, null);
    }


    /**
     * Исполняет операторы, выраженные узлами AST дерева. Работает рекурсивно для каждых дочерних узлов родительского узла.
     * @param mainNode корневой узел AST дерева
     * @param input выражение, которое нужно передать в condition
     * @return Возвращает результат работы.
     * @throws InterpreterException выбрасывается, если в AST дереве присутствуют ошибки.
     */
    public B execute(Node mainNode , T input, Map<String, B> localConditions) throws InterpreterException {
        if(mainNode instanceof TernaryOperatorNode ternaryOperatorNode) {
            List<Node> childrenNode = mainNode.getChildrenNodes().stream().toList();
            return listTernaryOperators.get(ternaryOperatorNode.getNodeName()).apply((Boolean) execute(childrenNode.get(0), input, localConditions), execute(childrenNode.get(1), input, localConditions), execute(childrenNode.get(2), input,localConditions));
        }
        else if(mainNode instanceof BinaryOperatorNode binaryOperatorNode) {
            List<Node> childrenNode = mainNode.getChildrenNodes().stream().toList();
            return listBinaryOperators.get(binaryOperatorNode.getNodeName()).apply(execute(childrenNode.get(0), input,localConditions), execute(childrenNode.get(1), input, localConditions));
        }
        else if(mainNode instanceof UnaryOperatorNode unaryOperationNode) {
            List<Node> childrenNode = mainNode.getChildrenNodes().stream().toList();
            return listUnaryOperators.get(unaryOperationNode.getNodeName()).apply(execute(childrenNode.get(0), input,localConditions));
        }
        else if(mainNode instanceof ConditionNode conditionNode) {
            if(!Objects.isNull(localConditions) && localConditions.containsKey(conditionNode.getNodeName())) return localConditions.get(conditionNode.getNodeName());
            if(listConditionNames.stream().anyMatch(cn ->conditionNode.getNodeName().matches(cn.getRegEx()))) {
                ConditionName conditionName = listConditionNames.stream().filter(cn ->conditionNode.getNodeName().matches(cn.getRegEx())).findFirst().orElse(null);
                assert conditionName != null;
                return conditionName.getCondition().apply(input, conditionNode.getNodeName());
            }
            else if(Objects.isNull(alternativeConditionParser)) throw new InterpreterException(String.format("Unknown condition: %s", mainNode.getNodeName()));
            else return alternativeConditionParser.apply(conditionNode.getNodeName());
        }
        else if(mainNode instanceof MethodOperatorNode methodOperatorNode) {
            B context = execute(methodOperatorNode.getContext(), input, localConditions);
            List<B> arguments = executeList(methodOperatorNode.getChildrenNodes(), input, localConditions);
            if(context instanceof WrapperObject<?> wrapperObject) {
                Object value = wrapperObject.executeMethod(methodOperatorNode.getNodeName(), arguments);
                return (B) value;
            }
            else throw new InterpreterException(String.format("Condition \"%s\" not be Object.", methodOperatorNode.getContext().getNodeName()));
        }
        else if(mainNode instanceof FunctionOperatorNode functionOperatorNode) {
            return listFunctionOperators.get(functionOperatorNode.getNodeName()).apply(executeList(mainNode.getChildrenNodes(), input, localConditions));
        }
        else throw new InterpreterException(String.format("Unknown node: %s", mainNode));
    }
    
    protected List<B> executeList(List<Node> nodeList, T input, Map<String, B> localConditions) throws InterpreterException {
        List<B> objectList = new ArrayList<>(nodeList.size());
        for (Node node : nodeList) {
            objectList.add(execute(node, input, localConditions));
        }
        return objectList;
    }

}
