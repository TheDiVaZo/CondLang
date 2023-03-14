package org.thedivazo.dicesystem.parserexpression.interpreter.wrapper;

import java.lang.constant.Constable;

public interface WrapperMethod<T, T1, V> extends Constable {
    String getMethodName();
    Class<?>[] getArgumentTypes();
    V execute(WrapperObject<T, T1> wrapperObjectContext, T1... arguments);

    boolean equals(String methodName, Class<?>[] argumentTypes);
}
