package org.thedivazo.condlang.interpreter.wrapper;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.constant.ConstantDesc;
import java.util.Arrays;
import java.util.Optional;

@RequiredArgsConstructor
@Getter
public abstract class AbstractWrapperMethod<T, V> implements WrapperMethod<T, V> {

    protected final String methodName;
    protected final Class<?>[] argumentTypes;

    @Override
    public boolean equals(String methodName, Class<?>[] argumentTypes) {
        return this.methodName.equals(methodName) && Arrays.equals(this.argumentTypes, argumentTypes);
    }

    @Override
    public Optional<? extends ConstantDesc> describeConstable() {
        return Optional.empty();
    }
}
