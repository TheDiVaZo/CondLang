package org.thedivazo.condlang;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.math.NumberUtils;
import org.junit.jupiter.api.Test;
import org.thedivazo.condlang.exception.CompileException;
import org.thedivazo.condlang.exception.InterpreterException;
import org.thedivazo.condlang.interpreter.WrapperManager;

import java.io.Serializable;
import java.lang.constant.Constable;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.thedivazo.condlang.TestVariables.parserExpression;

public class TestParser {
    void globalTest() throws CompileException, InterpreterException {
        Serializable code1 = parserExpression.compile("1+1-1+1-1+PI//2"); //2
        Serializable code2 = parserExpression.compile("---cos(PI/2)+signum(sin(3))"); //0.9999999999999999
        Serializable code3 = parserExpression.compile("cos(sin(cos(sin(cos(exp(3))))))"); //0.6877064801469988
        Serializable code4 = parserExpression.compile("pow(cos(PI/2),2)+pow(sin(PI/2),2) == 1 ? 4+3*cos(3)/sin(3) : PI"); //-17.045757654303603
        Serializable code5 = parserExpression.compile("random(10//2,6)"); //random number 5-6
        Serializable code6 = parserExpression.compile("max(11.214356,2,3.234567,4,(((((((((5))))))))),pow(sqrt(15),2),15)"); // 15.000000000000002
        Serializable code7 = parserExpression.compile("sqrt(((((((4))*2)/2)*2)/2))*2");//4
        Serializable code8 = parserExpression.compile("log(E,100)-ln(100)"); // 0
        Serializable code9 = parserExpression.compile("cos(random(0,10)) < 2 && sin(random(0,100)) > -2 ? 1:0"); //always 1
        Serializable code10 = parserExpression.compile("((((((2*random(5,5)))))))--4+1*6/2+-3*(((((((6*6)))))))-2+1"); //-92
        Serializable code11 = parserExpression.compile("cos(PI/2+3)>3 ? cos(0.8):sin(pow(sqrt(PI/2),2))==1 ? sqrt(9)*3==9 ? -228--3----3-6:1:1"); //-228
        Serializable code12 = parserExpression.compile("8.4+1.6-3.333333+2.9-1.0000000000000 + 3"); //11.566667
        Serializable code13 = parserExpression.compile("'test_string' == 'test_string'");
        Serializable code14 = parserExpression.compile("'test_string' == 'test_string' && 'test2_string' == 'test2_string'");
        Serializable code15 = parserExpression.compile("'result cos: '+cos(PI/2) == 'result cos: '+cos(PI/2)");
        Serializable code16 = parserExpression.compile("'random: '+random(0,100000) != 'random: '+random(0,100000)");
        Serializable code17 = parserExpression.compile("''+''+''+''+''+''+''+''+''+''+''+''+''+''+''+''+''+''+'' == ''");
        Serializable code18 = parserExpression.compile("1+'2'+3+'4'+5 == '12345'");
        Serializable code19 = parserExpression.compile("str(5)");
        Serializable code20 = parserExpression.compile("str('5')");
        Serializable code21 = parserExpression.compile("str('5'+'6')");
        Serializable code22 = parserExpression.compile("number('5')");
        Serializable code23 = parserExpression.compile("number(5)");
        Serializable code24 = parserExpression.compile("number('5'+'6')");
        Serializable code25 = parserExpression.compile("number(str(5))");
        Serializable code26 = parserExpression.compile("4.5//2");
        Serializable code27 = parserExpression.compile("4.5//2.5");
        Serializable code28 = parserExpression.compile("4//2");
        Serializable code29 = parserExpression.compile("5 - 4//2 + 3");
        Serializable code30 = parserExpression.compile("boolean(true)");
        Serializable code31 = parserExpression.compile("boolean('true')");
        Serializable code32 = parserExpression.compile("boolean(1)");
        Serializable code33 = parserExpression.compile("boolean('1')");
        Serializable code34 = parserExpression.compile("boolean(random(2,1000))");
        Serializable code35 = parserExpression.compile("boolean(false)");
        Serializable code36 = parserExpression.compile("boolean('false')");
        Serializable code37 = parserExpression.compile("boolean(0)");
        Serializable code38 = parserExpression.compile("boolean('0')");
        Serializable code39 = parserExpression.compile("emptyFunction()");
        Serializable code40 = parserExpression.compile("100 > 10 ? 1:0");

        assertEquals(2d, (double) parserExpression.execute(code1));
        assertEquals(0.9999999999999999d, (double) parserExpression.execute(code2));
        assertEquals(0.6877064801469988d, (double) parserExpression.execute(code3));
        assertEquals(-17.045757654303603d, (double) parserExpression.execute(code4));
        assertTrue((double) parserExpression.execute(code5) <= 6d || (double) parserExpression.execute(code5) >= 5d);
        assertEquals(15.000000000000002d, (double) parserExpression.execute(code6));
        assertEquals(4d, (double) parserExpression.execute(code7));
        assertEquals(0d, (double) parserExpression.execute(code8));
        assertEquals(1d, (double) parserExpression.execute(code9));
        assertEquals(-92d, (double) parserExpression.execute(code10));
        assertEquals(-228d, (double) parserExpression.execute(code11));
        assertEquals(11.566667d, (double) parserExpression.execute(code12));
        assertTrue((boolean) parserExpression.execute(code13));
        assertTrue((boolean) parserExpression.execute(code14));
        assertTrue((boolean) parserExpression.execute(code15));
        assertTrue((boolean) parserExpression.execute(code16));
        assertTrue((boolean) parserExpression.execute(code17));
        assertTrue((boolean) parserExpression.execute(code18));
        assertEquals("5",parserExpression.execute(code19));
        assertEquals("5",parserExpression.execute(code20));
        assertEquals("56",parserExpression.execute(code21));
        assertEquals(5d, (double) parserExpression.execute(code22));
        assertEquals(5d, (double) parserExpression.execute(code23));
        assertEquals(56d, (double) parserExpression.execute(code24));
        assertEquals(5d, (double) parserExpression.execute(code25));
        assertEquals(2d, (double) parserExpression.execute(code26));
        assertEquals(1d, (double) parserExpression.execute(code27));
        assertEquals(2d, (double) parserExpression.execute(code28));
        assertEquals(6d, (double) parserExpression.execute(code29));
        assertTrue((boolean)parserExpression.execute(code30));
        assertTrue((boolean)parserExpression.execute(code31));
        assertTrue((boolean)parserExpression.execute(code32));
        assertTrue((boolean)parserExpression.execute(code33));
        assertTrue((boolean)parserExpression.execute(code34));
        assertFalse((boolean)parserExpression.execute(code35));
        assertFalse((boolean)parserExpression.execute(code36));
        assertFalse((boolean)parserExpression.execute(code37));
        assertFalse((boolean)parserExpression.execute(code38));
        assertEquals(20d, parserExpression.execute(code39));
        assertEquals(1d, parserExpression.execute(code40));
    }

    @Test
    void variableTest() throws InterpreterException, CompileException {

        Map<String, Constable> variables = new HashMap<>();
        variables.put("variable.0.1", (Constable) 5d);
        variables.put("variable.0.2", "5");
        variables.put("variable.0.3", (Constable) true);
        variables.put("variable.0.4", parserExpression.execute("1+1"));
        variables.put("variable.0.5", (Constable) false);
        variables.put("variable.0.6", parserExpression.execute("false"));
        parserExpression.setCondition("[a-zA-Z0-9\\.]+");

        System.out.println(parserExpression.execute("$variable.0.1", NumberUtils.DOUBLE_ZERO, variables));
        assertEquals(5d,(Double) parserExpression.execute("$variable.0.1", NumberUtils.DOUBLE_ZERO, variables));
        assertEquals("5",parserExpression.execute("$variable.0.2", NumberUtils.DOUBLE_ZERO, variables));
        assertTrue((boolean) parserExpression.execute("$variable.0.3", NumberUtils.DOUBLE_ZERO, variables));
        assertEquals(2d,(double) parserExpression.execute("$variable.0.4", NumberUtils.DOUBLE_ZERO, variables));
        assertFalse((boolean) parserExpression.execute("$variable.0.5", NumberUtils.DOUBLE_ZERO, variables));
        assertFalse((boolean) parserExpression.execute("$variable.0.6", NumberUtils.DOUBLE_ZERO, variables));
    }

    @Test
    void objectTests() throws InterpreterException, CompileException {

        @RequiredArgsConstructor
        @Getter
        class Address {
            private final String city;
            private final String street;
            private final String homeNumber;
        }

        enum Sex {
            MALE,
            FEMALE
        }
        @Getter
        @RequiredArgsConstructor
        class Human {
            private final double age;
            private final String name;
            private final Sex sex;
            private final Address address;
        }

        Map<String, Constable> variables = new HashMap<>();
        variables.put("Putin", WrapperManager.generateWrapper(new Human(70, "Putin", Sex.MALE, new Address("Moscow", "the Red Square", "1")), Constable.class));
        parserExpression.setCondition("[a-zA-Z0-9]+");
        parserExpression.addMethod("[a-zA-Z0-9]+");

        String code1 = "$Putin#getName()";
        String code2 = "$Putin#getAge()";
        String code3 = "$Putin#getSex()";
        String code4 = "$Putin#getAddress()#getCity() + '|' + $Putin#getAddress()#getStreet()";

        assertEquals("Putin", parserExpression.execute(code1, NumberUtils.DOUBLE_ZERO, variables).toString());
        assertEquals(70d,(Double) parserExpression.execute(code2, NumberUtils.DOUBLE_ZERO, variables));
        assertEquals("MALE", parserExpression.execute(code3, NumberUtils.DOUBLE_ZERO, variables).toString());
        assertEquals("Moscow|the Red Square", parserExpression.execute(code4, NumberUtils.DOUBLE_ZERO, variables).toString());
    }
}
