package org.thedivazo.condlang;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.intellij.lang.annotations.RegExp;
import org.thedivazo.condlang.exception.CompileException;
import org.thedivazo.condlang.exception.InterpreterException;
import org.thedivazo.condlang.interpreter.Interpreter;
import org.thedivazo.condlang.interpreter.wrapper.WrapperObject;
import org.thedivazo.condlang.lexer.Lexer;
import org.thedivazo.condlang.lexer.TokenType;
import org.thedivazo.condlang.parser.Node;
import org.thedivazo.condlang.parser.OperatorType;
import org.thedivazo.condlang.parser.Parser;
import org.thedivazo.condlang.utils.TernFunction;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * <p>This class wrapped around  {@link Parser}, {@link Interpreter} and {@link Lexer}</p>
 * <p>With their help can do your own custom parser</p>
 * @param <T> The type that is the parent of the input data types
 * @param <R> The type of output data
 * @version 1.0
 */
@RequiredArgsConstructor
public class ParserExpression<T, R extends B, B> {

    @Getter(AccessLevel.PROTECTED)
    private final Lexer lexer = new Lexer();

    @Getter(AccessLevel.PROTECTED)
    private final Parser parser = new Parser();

    @Getter(AccessLevel.PROTECTED)
    private final Interpreter<T, R, B> interpreter = new Interpreter<>();


    /**
     * @param startVariableSymbols The characters that indicate the start of a local variable
     */
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
     * <p>adds support for the above ternary operator</p>
     * @param operatorData Object that indicate ternary operator
     */
    public void addTernaryOperator(TernaryOperatorWrapper<B> operatorData) {
        parser.addOperator(new Parser.OperatorData(operatorData.getSignOne(), OperatorType.TERNARY_1), new Parser.OperatorData(operatorData.getSignTwo(), OperatorType.TERNARY_2));
        lexer.putOperator(Pattern.quote(operatorData.getSignOne()),TokenType.OPERATOR);
        lexer.putOperator(Pattern.quote(operatorData.getSignTwo()),TokenType.OPERATOR);
        interpreter.addTernaryOperator(operatorData.getSignOne(), operatorData.getSignTwo(), operatorData.getTernaryOperator());
    }

    /**
     * <p>adds support for the above binary operator</p>
     * @param operatorsData Objects that indicate binary operator.
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
     * @param regEx строка, представляющая собой условие (переменную)
     * @param b
     */

    /**
     * @param regEx regEx относительно которого будет производиться поиск условий (переменных) в выражении
     * @param condition функция, являющееся обработчиком условия, принимающая на вход {@link String} и возвращающая R ({@link ParserExpression})
     */
    public void setCondition(@RegExp String regEx, BiFunction<T,String,B> condition) {
        lexer.putOperator(regEx, TokenType.CONDITION);
        interpreter.addCondition(regEx, condition);
    }

    public void setCondition(@RegExp String regEx) {
        lexer.putOperator(regEx, TokenType.CONDITION);
    }

    /**
     * Аналогичен {@link ParserExpression#setCondition(String, BiFunction)}, только возвращается статичное значение
     * @param regEx {@link ParserExpression#setCondition(String, BiFunction)}
     * @param result статичное значение R ({@link ParserExpression})
     */
    public void setCondition(@RegExp String regEx, R result) {
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


    public void addMethods(Set<String> methods) {
        for (@RegExp String method : methods) {
            addMethod(method);
        }
    }

    public void addMethod(@RegExp String method) {
        lexer.putOperator(method, TokenType.METHOD);
    }

    /**
     * Добавляет операторы для задания приоритета. Выражение между данными операторами будет самым приоритетным.
     * @param compoundStartSign первый оператор
     * @param compoundEndSign второй оператор
     */
    public void addCompoundOperators(@RegExp String compoundStartSign,@RegExp String compoundEndSign) {
        lexer.putOperator(compoundStartSign, TokenType.COMPOUND_START);
        lexer.putOperator(compoundEndSign, TokenType.COMPOUND_END);
    }

    /**
     * добавляет оператор разграничения аргументов для функции
     * @param delimiter знак оператора
     */
    public void addDelimiter(@RegExp String delimiter) {
        lexer.putOperator(delimiter, TokenType.DELIMITER);
    }

    /**
     * Добавляет символы, которые не должны учитываться при парсинге выражения.
     * @param skipSymbols список символов.
     */
    public void addSkipSymbols(String... skipSymbols) {
        for (@RegExp String skipSymbol : skipSymbols) {
            lexer.putOperator(skipSymbol, TokenType.SPACE);
        }
    }


    /**
     * Добавляет символы, которые позволяют получить доступ к методу condition
     * @param methodReferenceSymbols список символов.
     */
    public void addMethodReferenceSymbols(String... methodReferenceSymbols) {
        for (@RegExp String methodReferenceSymbol : methodReferenceSymbols) {
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
    public B execute(String code, T input, Map<String, B> localArguments) throws CompileException, InterpreterException {
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
