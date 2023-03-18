package org.thedivazo.condlang.interpreter;

import org.thedivazo.condlang.interpreter.wrapper.AbstractWrapperMethod;
import org.thedivazo.condlang.interpreter.wrapper.AbstractWrapperObject;
import org.thedivazo.condlang.interpreter.wrapper.WrapperMethod;
import org.thedivazo.condlang.interpreter.wrapper.WrapperObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class WrapperManager {
    public static <T> WrapperObject<T> generateWrapper(T object, Class<?> primitiveType) {
        return new AbstractWrapperObject<T>(object) {

            @Override
            protected void generateMethods() {
                T currentObject = getObject();
                Method[] methods = currentObject.getClass().getMethods();
                for (Method method : methods) {
                    if(!method.getReturnType().equals(Void.TYPE)) {
                        WrapperMethod<T, ?> wrapperMethod = new AbstractWrapperMethod<T, Object>(method.getName(), method.getParameterTypes()) {
                            @Override
                            public Object execute(WrapperObject<T> wrapperObjectContext, Object... arguments) {
                                T objectContext = wrapperObjectContext.getObject();
                                Class<?> clazzObjectContext = objectContext.getClass();
                                try {
                                    Method methodContext = clazzObjectContext.getMethod(getMethodName(), getArgumentTypes());
                                    methodContext.setAccessible(true);
                                    Object result = methodContext.invoke(objectContext, arguments);
                                    if(primitiveType.isInstance(result)) return result;
                                    else return generateWrapper(result, primitiveType);

                                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                                    return null; //2928
                                }
                            }
                        };
                        wrapperMethodSet.add(wrapperMethod);
                    }
                }

            }
        };
    }

    public static Set<String> getMethodsObject(Class<?> clazz) {
        return Arrays.stream(clazz.getMethods()).map(Method::getName).collect(Collectors.toSet());
    }
}
