package org.thedivazo.condlang.utils;

public interface TernFunction<T, U, D, R> {
    R apply(T t, U u, D d);
}
