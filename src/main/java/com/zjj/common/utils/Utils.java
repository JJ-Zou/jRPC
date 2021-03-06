package com.zjj.common.utils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class Utils {
    private Utils() {
    }

    public static boolean isGetter(Method method) {
        String name = method.getName();
        if (!name.startsWith("get") && !name.startsWith("is")) {
            return false;
        }
        if (name.equals("get") || name.equals("is")) {
            return false;
        }
        if (name.equals("getClass") || name.equals("getObject")) {
            return false;
        }
        if (!Modifier.isPublic(method.getModifiers())) {
            return false;
        }
        if (method.getParameterCount() != 0) {
            return false;
        }
        if (method.getReturnType().isPrimitive()) {
            return false;
        }
        return true;
    }

    public static boolean isSetter(Method method) {
        String name = method.getName();
        if (!name.startsWith("set")) {
            return false;
        }
        if (name.equals("set")) {
            return false;
        }
        if (name.equals("setClass") || name.equals("setObject")) {
            return false;
        }
        if (!Modifier.isPublic(method.getModifiers())) {
            return false;
        }
        if (method.getParameterCount() != 1) {
            return false;
        }
        if (method.getReturnType() != void.class) {
            return false;
        }
        return true;
    }

    public static String getPropertyFromSetter(Method method) {
        String setter = method.getName();
        return setter.substring(3, 4).toLowerCase() + setter.substring(4);
    }

}
