package com.zjj.common.function;

@FunctionalInterface
public interface ThrowableFunction<T, R> {
    R apply(T t) throws Throwable;

    default R execute(T t) throws RuntimeException {
        R result = null;
        try {
            result = apply(t);
        } catch (Throwable e) {
            throw new RuntimeException(e.getCause());
        }
        return result;
    }

    static <T, R> R execute(T t, ThrowableFunction<T, R> function) {
        return function.execute(t);
    }
}
