package org.thedivazo.condlang;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.thedivazo.condlang.interpreter.Interpreter;
import org.thedivazo.condlang.lexer.Lexer;
import org.thedivazo.condlang.parser.Parser;
import org.thedivazo.condlang.utils.TernFunction;

import java.lang.constant.Constable;
import java.lang.constant.ConstantDesc;
import java.lang.constant.ConstantDescs;
import java.lang.invoke.ConstantBootstraps;
import java.math.BigDecimal;
import java.util.function.BiFunction;
import java.util.function.Function;

public class TestVariables {
    public static final ParserExpression<Constable, Constable, Constable> parserExpression = new ParserExpression<>();
    static {
        parserExpression.setCondition("'.*?'", (input, string)->string.substring(1,string.length()-1));

        //arithmetic operators
        // unary "-"
        parserExpression.addUnaryOperator(
                new ParserExpression.UnaryOperatorWrapper<>() {
                    @Override
                    public String getSign() {
                        return "-";
                    }

                    @Override
                    public Function<Constable, Constable> getUnaryOperator() {
                        return object -> {
                            if(NumberUtils.isCreatable(object.toString())) return Double.valueOf(-NumberUtils.createDouble(object.toString()));
                            else return BooleanUtils.toBooleanObject(object.toString());
                        };
                    }
                });
        // "*" and and "//" and "/" and "%"
        parserExpression.addBinaryOperator(
                new ParserExpression.BinaryOperatorWrapper<>() {
                    @Override
                    public String getSign() {
                        return "*";
                    }

                    @Override
                    public BiFunction<Constable, Constable, Constable> getBinaryOperator() {
                        return (aDouble, aDouble2) -> (Double) ((Double) aDouble *  (Double) aDouble2);
                    }
                },
                new ParserExpression.BinaryOperatorWrapper<>() {
                    @Override
                    public String getSign() {
                        return "//";
                    }

                    @Override
                    public BiFunction<Constable, Constable, Constable> getBinaryOperator() {
                        return (aDouble, aDouble2) ->Double.valueOf((int) ((double) aDouble / (double) aDouble2));
                    }
                },
                new ParserExpression.BinaryOperatorWrapper<>() {
                    @Override
                    public String getSign() {
                        return "/";
                    }

                    @Override
                    public BiFunction<Constable, Constable, Constable> getBinaryOperator() {
                        return (aDouble, aDouble2) ->(Double)((Double) aDouble / (Double) aDouble2);
                    }
                },
                new ParserExpression.BinaryOperatorWrapper<>() {
                    @Override
                    public String getSign() {
                        return "%";
                    }

                    @Override
                    public BiFunction<Constable, Constable, Constable> getBinaryOperator() {
                        return (aDouble, aDouble2) ->(Double) ((Double) aDouble % (Double) aDouble2);
                    }
                });
        // "+" and "-"
        parserExpression.addBinaryOperator(
                new ParserExpression.BinaryOperatorWrapper<>() {
                    @Override
                    public String getSign() {
                        return "+";
                    }

                    @Override
                    public BiFunction<Constable, Constable, Constable> getBinaryOperator() {
                        return (object1, object2)->{
                            if(object1 instanceof Double double1 && object2 instanceof Double double2)
                                return Double.valueOf(double1+double2);
                            else if(object1 instanceof Double double1) return NumberUtils.toScaledBigDecimal(double1).stripTrailingZeros().toPlainString() + object2.toString();
                            else if(object2 instanceof Double double2) return object1.toString() + NumberUtils.toScaledBigDecimal(double2).stripTrailingZeros().toPlainString();
                            else return object1.toString()+object2.toString();
                        };
                    }
                },
                new ParserExpression.BinaryOperatorWrapper<>() {
                    @Override
                    public String getSign() {
                        return "-";
                    }

                    @Override
                    public BiFunction<Constable, Constable, Constable> getBinaryOperator() {
                        return (aDouble, aDouble2) ->(Double) ((Double) aDouble - (Double) aDouble2);
                    }
                });

        // "<=" and ">=" and "<" and ">"
        parserExpression.addBinaryOperator(
                new ParserExpression.BinaryOperatorWrapper<>() {
                    @Override
                    public String getSign() {
                        return "<=";
                    }

                    @Override
                    public BiFunction<Constable, Constable, Constable> getBinaryOperator() {
                        return (dob1, dob2)->(Boolean)((double) dob1 <= (double) dob2);
                    }
                },
                new ParserExpression.BinaryOperatorWrapper<>() {
                    @Override
                    public String getSign() {
                        return ">=";
                    }

                    @Override
                    public BiFunction<Constable, Constable, Constable> getBinaryOperator() {
                        return (dob1, dob2)->(Boolean)((double) dob1 >= (double) dob2);
                    }
                },
                new ParserExpression.BinaryOperatorWrapper<>() {
                    @Override
                    public String getSign() {
                        return "<";
                    }

                    @Override
                    public BiFunction<Constable, Constable, Constable> getBinaryOperator() {
                        return (dob1, dob2)->(Boolean)((double) dob1 < (double) dob2);
                    }
                },
                new ParserExpression.BinaryOperatorWrapper<>() {
                    @Override
                    public String getSign() {
                        return ">";
                    }

                    @Override
                    public BiFunction<Constable, Constable, Constable> getBinaryOperator() {
                        return (dob1, dob2)->(Boolean)((double) dob1 > (double) dob2);
                    }
                });
        // "==" and "!="
        parserExpression.addBinaryOperator(
                new ParserExpression.BinaryOperatorWrapper<>() {
                    @Override
                    public String getSign() {
                        return "==";
                    }

                    @Override
                    public BiFunction<Constable, Constable, Constable> getBinaryOperator() {
                        return (obj1, obj2)-> (Boolean)(obj1.equals(obj2));
                    }
                },
                new ParserExpression.BinaryOperatorWrapper<>() {
                    @Override
                    public String getSign() {
                        return "!=";
                    }

                    @Override
                    public BiFunction<Constable, Constable, Constable> getBinaryOperator() {
                        return (obj1, obj2)->(Boolean)!obj1.equals(obj2);
                    }
                });

        //Boolean operators
        // unary "!"
        parserExpression.addUnaryOperator(
                new ParserExpression.UnaryOperatorWrapper<>() {
                    @Override
                    public String getSign() {
                        return "!";
                    }

                    @Override
                    public Function<Constable, Constable> getUnaryOperator() {
                        return (anyObject)->{
                            if(anyObject instanceof Double doubleObject) return Double.valueOf(-doubleObject);
                            else return  (Boolean)!(Boolean) (anyObject);
                        };
                    }
                });
        // "&&"
        parserExpression.addBinaryOperator(
                new ParserExpression.BinaryOperatorWrapper<>() {
                    @Override
                    public String getSign() {
                        return "&&";
                    }

                    @Override
                    public BiFunction<Constable, Constable, Constable> getBinaryOperator() {
                        return (bol1, bol2)->Boolean.valueOf((boolean) bol1 && (boolean) bol2);
                    }
                });
        // "||"
        parserExpression.addBinaryOperator(
                new ParserExpression.BinaryOperatorWrapper<>() {
                    @Override
                    public String getSign() {
                        return "||";
                    }

                    @Override
                    public BiFunction<Constable, Constable, Constable> getBinaryOperator() {
                        return (bol1, bol2)->Boolean.valueOf((boolean) bol1 || (boolean) bol2);
                    }
                });


        //Ternary condition operator " : ? "
        parserExpression.addTernaryOperator(
                new ParserExpression.TernaryOperatorWrapper<>() {
                    @Override
                    public String getSignOne() {
                        return "?";
                    }

                    @Override
                    public String getSignTwo() {
                        return ":";
                    }

                    @Override
                    public TernFunction<Boolean, Constable, Constable, Constable> getTernaryOperator() {
                        return (cond1, cond2, cond3) -> cond1 ? cond2:cond3;
                    }
                });

        //function
        parserExpression.setFunction("cos", doubles -> (Constable) Math.cos((double) doubles.get(0)));
        parserExpression.setFunction("sin", doubles -> (Constable) Math.sin((double) doubles.get(0)));
        parserExpression.setFunction("tg", doubles -> (Constable) Math.tan((double) doubles.get(0)));
        parserExpression.setFunction("ctg", doubles -> (Constable) Math.tan((double) doubles.get(0)));

        parserExpression.setFunction("abs", doubles -> (Constable) Math.abs((double) doubles.get(0)));
        parserExpression.setFunction("floor", doubles -> (Constable) Math.floor((double) doubles.get(0)));
        parserExpression.setFunction("round", doubles -> (Constable) Math.round((double) doubles.get(0)));
        parserExpression.setFunction("ceil", doubles -> (Constable) Math.ceil((double) doubles.get(0)));
        parserExpression.setFunction("rint", doubles -> (Constable) Math.rint((double) doubles.get(0)));
        parserExpression.setFunction("copySign", doubles -> (Constable) Math.copySign((double) doubles.get(0), (double) doubles.get(1)));

        parserExpression.setFunction("ln", doubles -> (Constable) Math.log((double) doubles.get(0)));
        parserExpression.setFunction("exp", doubles -> (Constable) Math.exp((double) doubles.get(0)));
        parserExpression.setFunction("log", doubles -> (Constable) (Math.log((double) doubles.get(1))/Math.log((double) doubles.get(0))));
        parserExpression.setFunction("pow", doubles -> (Constable) Math.pow((double) doubles.get(0),(double) doubles.get(1)));
        parserExpression.setFunction("sqrt", doubles -> (Constable) Math.sqrt((double) doubles.get(0)));
        parserExpression.setFunction("cbrt", doubles -> (Constable) Math.cbrt((double) doubles.get(0)));


        parserExpression.setFunction("random", doubles -> (Constable) (Math.random()*((double) doubles.get(1)-(double) doubles.get(0))+(double) doubles.get(0)));

        parserExpression.setFunction("max", doubles -> (Constable) NumberUtils.max(doubles.stream().mapToDouble(Double.class::cast).toArray()));
        parserExpression.setFunction("min", doubles -> (Constable) NumberUtils.min(doubles.stream().mapToDouble(Double.class::cast).toArray()));

        parserExpression.setFunction("signum", doubles -> (Constable) Math.signum((double) doubles.get(0)));

        parserExpression.setFunction("str", objects->{
            Object constable = objects.get(0);
            if(constable instanceof Double double1) return BigDecimal.valueOf(double1).stripTrailingZeros().toPlainString();
            else return constable.toString();
        });
        parserExpression.setFunction("number", objects->{
            Object constable = objects.get(0);
            if(NumberUtils.isCreatable(constable.toString())) return NumberUtils.createDouble(constable.toString());
            else if(constable instanceof Boolean boolean1) return (Constable) ((boolean1) ? 1 : 0);
            else return NumberUtils.DOUBLE_ZERO;
        });
        parserExpression.setFunction("boolean", objects->{
            Object constable = objects.get(0);
            if(constable instanceof Boolean boolean1) return boolean1;
            else if(constable instanceof Double double1) return (Constable) BooleanUtils.toBoolean((int) double1.doubleValue());
            else return (Constable) BooleanUtils.toBoolean(constable.toString());
        });

        parserExpression.setFunction("emptyFunction", (input)-> (Constable) 20d);


        parserExpression.setCondition("true", Boolean.TRUE);
        parserExpression.setCondition("false", Boolean.FALSE);

        //Condition and constant
        parserExpression.setCondition("PI",Double.valueOf(Math.PI));
        parserExpression.setCondition("E", Double.valueOf(Math.E));

        parserExpression.setCondition("[0-9]+(\\.[0-9]+)?", (player, sign)->NumberUtils.createDouble(sign));

        parserExpression.setAlternativeConditionParser(NumberUtils::createDouble);
        parserExpression.addDelimiter("\\,");
        parserExpression.addSkipSymbols(" +");
        parserExpression.addCompoundOperators("\\(","\\)");

        parserExpression.addVariableStartSymbols("\\$");

        parserExpression.addMethodReferenceSymbols("#");

    }
}
