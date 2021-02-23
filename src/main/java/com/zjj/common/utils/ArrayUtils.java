package com.zjj.common.utils;

public class ArrayUtils {
    private ArrayUtils() {
    }

    public static boolean isNotEmpty(final Object[] array) {
        return !isEmpty(array);
    }

    public static boolean isEmpty(final Object[] array) {
        return array == null || array.length == 0;
    }
}
