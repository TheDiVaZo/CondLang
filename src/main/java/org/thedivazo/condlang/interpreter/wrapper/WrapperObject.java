package org.thedivazo.condlang.interpreter.wrapper;

import org.thedivazo.condlang.exception.InterpreterException;

import java.lang.constant.Constable;
import java.util.Set;

public interface WrapperObject<T> extends Constable {
    T getObject();
    Class<? extends T> getClassObject();

    Set<String> getMethodsName();
    boolean hasMethod(String nameMethod, Class<?>... methodArgumentsType);

    Object executeMethod(String nameMethod, Object... methodArguments) throws InterpreterException;
}
