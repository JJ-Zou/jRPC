package com.zjj.common.function;

@FunctionalInterface
public interface ThrowableConsumer<T> {
    void accept(T t) throws Throwable;

    default void execute(T t) throws RuntimeException {
        try {
            accept(t);
        } catch (Throwable e) {
            throw new RuntimeException(e.getCause());
        }
    }

    static <T> void execute(T t, ThrowableConsumer<T> consumer) {
         consumer.execute(t);
    }
}
