package org.thedivazo.condlang.interpreter;

import org.thedivazo.condlang.interpreter.wrapper.AbstractWrapperMethod;
import org.thedivazo.condlang.interpreter.wrapper.AbstractWrapperObject;
import org.thedivazo.condlang.interpreter.wrapper.WrapperMethod;
import org.thedivazo.condlang.interpreter.wrapper.WrapperObject;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class WrapperManager {

    protected static <T> WrapperMethod<T, Object> generateWrapperMethod(String methodName, Class<?>[] parameterTypes, Class<?> primitiveType) {

        return new AbstractWrapperMethod<T, Object>(methodName, parameterTypes) {
            @Override
            public Object execute(WrapperObject<T> wrapperObjectContext, Object... arguments) {
                T objectContext = wrapperObjectContext.getObject();
                Class<?> clazzObjectContext = wrapperObjectContext.getClassObject();
                try {
                    Method methodContext = clazzObjectContext.getMethod(getMethodName(), getArgumentTypes());
                    methodContext.setAccessible(true);
                    Object result = Modifier.isStatic(methodContext.getModifiers()) ? methodContext.invoke(null, arguments) : methodContext.invoke(objectContext, arguments);
                    if(primitiveType.isInstance(result)) return result;
                    else return generateWrapperObject(result, result.getClass(), primitiveType);

                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                    return null; //2928
                }
            }
        };

    }

    public static <T> WrapperObject<T> generateWrapperObject(T object, Class<? extends T> classObject, Class<?> primitiveType) {
        return new AbstractWrapperObject<T>(object, classObject) {

            @Override
            protected void generateMethods() {
                Method[] methods = getClassObject().getMethods();
                for (Method method : methods) {
                    if(!method.getReturnType().equals(Void.TYPE)) {
                        wrapperMethodSet.add(generateWrapperMethod(method.getName(), method.getParameterTypes(), primitiveType));
                    }
                }

    }
};
    }

    public static Set<String> getMethodsObject(Class<?> clazz) {
        return Arrays.stream(clazz.getMethods()).map(Method::getName).collect(Collectors.toSet());
    }
}
