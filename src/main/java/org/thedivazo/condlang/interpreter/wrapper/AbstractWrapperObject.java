package org.thedivazo.condlang.interpreter.wrapper;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.thedivazo.condlang.exception.InterpreterException;

import java.lang.constant.ConstantDesc;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractWrapperObject<T> implements WrapperObject<T> {

    @Getter
    protected final T object;

    protected final Set<WrapperMethod<T, ?>> wrapperMethodSet = new HashSet<>();

    protected AbstractWrapperObject(T object) {
        this.object = object;
        generateMethods();
    }

    @Override
    public Set<String> getMethodsName() {
        return wrapperMethodSet.stream().map(WrapperMethod::getMethodName).collect(Collectors.toSet());
    }

    @Override
    public boolean hasMethod(String nameMethod, Class<?>... methodArgumentsType) {
        return wrapperMethodSet.stream().anyMatch(tWrapperMethod -> tWrapperMethod.equals(nameMethod, methodArgumentsType));
    }

    @Override
    public Object executeMethod(String nameMethod, Object... methodArguments) throws InterpreterException {
        Optional<WrapperMethod<T, ?>> wrapperMethod = wrapperMethodSet.stream().filter(
                tWrapperMethod -> tWrapperMethod.equals(nameMethod, Arrays.stream(methodArguments).map(Object::getClass).toList().toArray(new Class[0])))
                .findFirst();
        if(wrapperMethod.isEmpty()) return null;
        else return wrapperMethod.get().execute(this, methodArguments);
    }

    @Override
    public Optional<? extends ConstantDesc> describeConstable() {
        return Optional.empty();
    }

    protected abstract void generateMethods();
}
