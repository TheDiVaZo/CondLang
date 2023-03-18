package org.thedivazo.condlang.interpreter.wrapper;

import java.lang.constant.Constable;

public interface WrapperMethod<T, V> extends Constable {
    String getMethodName();
    Class<?>[] getArgumentTypes();
    V execute(WrapperObject<T> wrapperObjectContext, Object... arguments);

    boolean equals(String methodName, Class<?>[] argumentTypes);
}
