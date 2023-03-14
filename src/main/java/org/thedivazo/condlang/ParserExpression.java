package org.thedivazo.dicesystem.parserexpression;

import lombok.RequiredArgsConstructor;
import org.thedivazo.dicesystem.parserexpression.exception.InterpreterException;
import org.thedivazo.dicesystem.parserexpression.lexer.Lexer;
import org.thedivazo.dicesystem.parserexpression.lexer.TokenType;
import org.thedivazo.dicesystem.parserexpression.parser.Parser;
import org.thedivazo.dicesystem.utils.TernFunction;
import org.thedivazo.dicesystem.parserexpression.exception.CompileException;
import org.thedivazo.dicesystem.parserexpression.interpreter.Interpreter;
import org.thedivazo.dicesystem.parserexpression.interpreter.wrapper.WrapperMethod;
import org.thedivazo.dicesystem.parserexpression.interpreter.wrapper.WrapperObject;
import org.thedivazo.dicesystem.parserexpression.parser.Node;
import org.thedivazo.dicesystem.parserexpression.parser.OperatorType;

import java.io.Serializable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Данный класс является оболочкой и объединением для {@link Parser}, {@link Interpreter} и {@link Lexer}
 * Он позволяет создать полностью настраеваемый парсер для ваших нужд без ручного управления отдельными частями парсера.
 * @param <T> Тип, который является входными данными
 * @param <R> Тип, с которым работает парсер. Так же он является выходными данными
 * @version 1.0
 */
@RequiredArgsConstructor
public class ParserExpression<T, R extends B, B> {
    private final Lexer lexer = new Lexer();
    private final Parser parser = new Parser();
    private final Interpreter<T, R, B> interpreter = new Interpreter<>();

    public void addVariableStartSymbols(String... startVariableSymbols) {
        for (String startVariableSymbol : startVariableSymbols) {
            lexer.putOperator(startVariableSymbol, TokenType.START_VARIABLE_SYMBOL);
        }
    }

    public interface TernaryOperatorWrapper<V> {
        String getSignOne();
        String getSignTwo();
        TernFunction<Boolean, V, V, V> getTernaryOperator();
    }

    public interface BinaryOperatorWrapper<V, D> {
        String getSign();
        BiFunction<V, V, D> getBinaryOperator();
    }
    public interface UnaryOperatorWrapper<V, D> {
        String getSign();
        Function<V, D> getUnaryOperator();
    }

    /**
     * На данный момент парсер не поддерживает добавление нескольких тернарных операторов с одинаковым приоритетом по отношению друг к другу.
     * @param operatorData объект, представляющий единственный тернарный оператор
     */
    public void addTernaryOperator(TernaryOperatorWrapper<B> operatorData) {
        parser.addOperator(new Parser.OperatorData(operatorData.getSignOne(), OperatorType.TERNARY_1), new Parser.OperatorData(operatorData.getSignTwo(), OperatorType.TERNARY_2));
        lexer.putOperator(Pattern.quote(operatorData.getSignOne()),TokenType.OPERATOR);
        lexer.putOperator(Pattern.quote(operatorData.getSignTwo()),TokenType.OPERATOR);
        interpreter.addTernaryOperator(operatorData.getSignOne(), operatorData.getSignTwo(), operatorData.getTernaryOperator());
    }

    /**
     * @param operatorsData группа приоритетно-равных по отношению друг к другу бинарных операторов.
     */
    @SafeVarargs
    public final void addBinaryOperator(BinaryOperatorWrapper<B, R>... operatorsData) {
        parser.addOperator(
                Arrays
                        .stream(operatorsData)
                        .map(binaryOperatorWrapper -> new Parser.OperatorData(binaryOperatorWrapper.getSign(), OperatorType.BINARY))
                        .toList()
                        .toArray(new Parser.OperatorData[]{}));
        for (BinaryOperatorWrapper<B, R> operatorData : operatorsData) {
            lexer.putOperator(Pattern.quote(operatorData.getSign()), TokenType.OPERATOR);
            interpreter.addBinaryOperator(operatorData.getSign(),operatorData.getBinaryOperator());
        }
    }


    /**
     * @param operatorsData группа приоритетно-равных по отношению друг к другу унарных операторов.
     */
    @SafeVarargs
    public final void addUnaryOperator(UnaryOperatorWrapper<B, R>... operatorsData) {
        parser.addOperator(
                Arrays
                        .stream(operatorsData)
                        .map(unaryOperatorWrapper -> new Parser.OperatorData(unaryOperatorWrapper.getSign(), OperatorType.UNARY))
                        .toList()
                        .toArray(new Parser.OperatorData[]{}));
        for (UnaryOperatorWrapper<B, R> operatorData : operatorsData) {
            lexer.putOperator(Pattern.quote(operatorData.getSign()), TokenType.OPERATOR);
            interpreter.addUnaryOperator(operatorData.getSign(),operatorData.getUnaryOperator());
        }
    }

    /**
     * @param sign строка, представляющая собой условие (переменную)
     */
    public void setCondition(String sign) {
        lexer.putOperator(sign, TokenType.CONDITION);
        //interpreter.addCondition(sign, null);
    }

    /**
     * @param regEx regEx относительно которого будет производиться поиск условий (переменных) в выражении
     * @param condition функция, являющееся обработчиком условия, принимающая на вход {@link String} и возвращающая R ({@link ParserExpression})
     */
    public void setCondition(String regEx, BiFunction<T,String,B> condition) {
        lexer.putOperator(regEx, TokenType.CONDITION);
        interpreter.addCondition(regEx, condition);
    }

    /**
     * Аналогичен {@link ParserExpression#setCondition(String, BiFunction)}, только возвращается статичное значение
     * @param regEx {@link ParserExpression#setCondition(String, BiFunction)}
     * @param result статичное значение R ({@link ParserExpression})
     */
    public void setCondition(String regEx, R result) {
        setCondition(regEx, (arg1, arg2)->result);
    }

    /**
     * Добавляет функцию вида "function(x)" в парсер
     * @param sign название функции
     * @param function тело функции
     */
    public void setFunction(String sign, Function<List<B>,R> function) {
        lexer.putOperator(sign, TokenType.FUNCTION);
        //parser.addNumberFunctionArgument(sign, argumentCompare);
        interpreter.addFunctionOperator(sign, function);
    }


    public void addObjects(WrapperObject<?>... wrapperObjects) {
        for (WrapperObject<?> wrapperObject : wrapperObjects) {
            for (WrapperMethod<?> method : wrapperObject.getMethods()) {
                lexer.putOperator(method.getMethodName(), TokenType.METHOD);
            }
        }
    }

    /**
     * Добавляет операторы для задания приоритета. Выражение между данными операторами будет самым приоритетным.
     * @param compoundStartSign первый оператор
     * @param compoundEndSign второй оператор
     */
    public void addCompoundOperators(String compoundStartSign, String compoundEndSign) {
        lexer.putOperator(compoundStartSign, TokenType.COMPOUND_START);
        lexer.putOperator(compoundEndSign, TokenType.COMPOUND_END);
    }

    /**
     * добавляет оператор разграничения аргументов для функции
     * @param delimiter знак оператора
     */
    public void addDelimiter(String delimiter) {
        lexer.putOperator(delimiter, TokenType.DELIMITER);
    }

    /**
     * Добавляет символы, которые не должны учитываться при парсинге выражения.
     * @param skipSymbols список символов.
     */
    public void addSkipSymbols(String... skipSymbols) {
        for (String skipSymbol : skipSymbols) {
            lexer.putOperator(skipSymbol, TokenType.SPACE);
        }
    }


    /**
     * Добавляет символы, которые позволяют получить доступ к методу condition
     * @param methodReferenceSymbols список символов.
     */
    public void addMethodReferenceSymbols(String... methodReferenceSymbols) {
        for (String methodReferenceSymbol : methodReferenceSymbols) {
            lexer.putOperator(methodReferenceSymbol, TokenType.METHOD_REFERENCE);
        }
    }

    /**
     * Альтернативный обработчик условий.
     * Если в выражении есть условие, у которого нет своего обработчика, то оно будет обработано данным обработчиком
     * @param alternativeConditionParser функция, принимающая {@link String} на вход и возвращающая R ({@link ParserExpression})
     */
    public void setAlternativeConditionParser(Function<String, R> alternativeConditionParser) {
        interpreter.setAlternativeConditionParser(alternativeConditionParser);
    }

    /**
     * @param code код, который нужно выполнить
     * @param input входные данные
     * @param localArguments локальные аргументы (условие, переменные). Они будут обработаны в первую очередь.
     * @return Возвращает значение R ({@link ParserExpression})
     * @throws CompileException исключение, генерируемое при возникновении ошибки компиляции
     * @throws InterpreterException исключение, генерируемое при возникновении ошибки выполнения
     */
    public Object execute(String code, T input, Map<String, B> localArguments) throws CompileException, InterpreterException {
        return interpreter.execute(parser.parsing(lexer.analyze(code)),input, localArguments);
    }

    /**
     * @param objectNode откомпилированный объект, представляющий собой результат работы {@link ParserExpression#compile(String)}
     * @param input входные данные
     * @param localArguments локальные аргументы (условие, переменные). Они будут обработаны в первую очередь.
     * @return Возвращает значение R ({@link ParserExpression})
     * @throws InterpreterException исключение, генерируемое при возникновении ошибки выполнения
     */
    public B execute(Serializable objectNode, T input, Map<String, B> localArguments) throws InterpreterException {
        if(!(objectNode instanceof Node nodeMain)) throw new IllegalArgumentException("This object is not a code to be executed");
        return interpreter.execute(nodeMain ,input, localArguments);
    }

    /**
     * Аналогичен {@link ParserExpression#execute(Serializable, Object, Map)}, но только без локальных аргументов
     * @param objectNode {@link ParserExpression#execute(Serializable, Object, Map)}
     * @param input {@link ParserExpression#execute(Serializable, Object, Map)}
     * @return {@link ParserExpression#execute(Serializable, Object, Map)}
     * @throws InterpreterException {@link ParserExpression#execute(Serializable, Object, Map)}
     */
    public B execute(Serializable objectNode, T input) throws InterpreterException {
        if(!(objectNode instanceof Node nodeMain)) throw new IllegalArgumentException("This object is not a code to be executed");
        return interpreter.execute(nodeMain, input);
    }

    /**
     * Аналогичен {@link ParserExpression#execute(String, Object, Map)}, но только без локальных аргументов
     * @param code {@link ParserExpression#execute(String, Object, Map)}
     * @param input {@link ParserExpression#execute(String, Object, Map)}
     * @return {@link ParserExpression#execute(String, Object, Map)}
     */
    public B execute(String code, T input) throws CompileException, InterpreterException {
        return interpreter.execute(parser.parsing(lexer.analyze(code)),input);
    }

    /**
     * Аналогичен {@link ParserExpression#execute(String, Object)}, но только без входных данных
     * @param code {@link ParserExpression#execute(String, Object, Map)}
     * @return {@link ParserExpression#execute(String, Object, Map)}
     */
    public B execute(String code) throws CompileException, InterpreterException {
        return interpreter.execute(parser.parsing(lexer.analyze(code)),null);
    }

    /**
     * Аналогичен {@link ParserExpression#execute(Serializable, Object)}, но только без входных данных
     * @param objectNode {@link ParserExpression#execute(Serializable, Object)}
     * @return {@link ParserExpression#execute(Serializable, Object)}
     */
    public B execute(Serializable objectNode) throws InterpreterException {
        if(!(objectNode instanceof Node nodeMain)) throw new IllegalArgumentException("This object is not a code to be executed");
        return interpreter.execute(nodeMain, null);
    }

    /**
     * компилирует код для последующего использования в {@link ParserExpression#execute(Serializable, Object, Map)}
     * @param code код
     * @return Возвращает объект, представляющий собой AST дерево. Данный код можно безопасно хранить и передавать.
     */
    public Serializable compile(String code) throws CompileException {
        return parser.parsing(lexer.analyze(code));
    }
}
