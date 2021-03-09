package com.zjj.common.utils;

import com.zjj.common.JRpcURLParamType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public class ReflectUtils {
    private ReflectUtils() {
    }

    private static final Map<String, Class<?>> NAME_CLASS_MAP = new ConcurrentHashMap<>();

    static {
        NAME_CLASS_MAP.put("boolean", boolean.class);
        NAME_CLASS_MAP.put("byte", byte.class);
        NAME_CLASS_MAP.put("char", char.class);
        NAME_CLASS_MAP.put("short", short.class);
        NAME_CLASS_MAP.put("int", int.class);
        NAME_CLASS_MAP.put("long", long.class);
        NAME_CLASS_MAP.put("float", float.class);
        NAME_CLASS_MAP.put("double", double.class);
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
        if (Iterator.class.isAssignableFrom(method.getReturnType())) {
            return false;
        }
        if (Map.class.isAssignableFrom(method.getReturnType())) {
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
        if (Modifier.isStatic(method.getModifiers())) {
            return false;
        }
        if (method.getParameterCount() != 1) {
            return false;
        }
        if (method.getReturnType() != void.class) {
            return false;
        }
        if (Iterator.class.isAssignableFrom(method.getParameterTypes()[0])) {
            return false;
        }
        if (Map.class.isAssignableFrom(method.getParameterTypes()[0])) {
            return false;
        }
        return true;
    }

    /**
     * 获取setter方法参数对应的字段名称
     *
     * @param method setter方法
     * @return 字段名称
     */
    public static String getPropertyFromSetter(Method method) {
        String setter = method.getName();
        return setter.substring(3, 4).toLowerCase() + setter.substring(4);
    }

    /**
     * 获取getter方法参数对应的字段名称
     *
     * @param method getter方法
     * @return 字段名称
     */
    public static String getPropertyFromGetter(Method method) {
        String getter = method.getName();
        int prefix = getter.startsWith("get") ? 3 : 2;
        return getter.substring(prefix, prefix + 1).toLowerCase() + getter.substring(prefix + 1);
    }

    public static String getMethodSign(String methodName, String paramSigns) {
        return StringUtils.isEmpty(paramSigns) ? methodName + "()" : methodName + "(" + paramSigns + ")";
    }

    /**
     * 获取方法签名: 方法名(参数类型,参数类型,...)
     *
     * @param method 方法
     * @return 方法签名
     */
    public static String getMethodSign(Method method) {
        return method.getName() + "(" + getParamSigns(method) + ")";
    }

    /**
     * 获取方法的参数类型: 参数类型,参数类型,...
     *
     * @param method 方法
     * @return 方法的参数类型
     */
    public static String getParamSigns(Method method) {
        StringBuilder builder = new StringBuilder();
        for (Class<?> parameterType : method.getParameterTypes()) {
            builder.append(getClassName(parameterType)).append(",");
        }
        int length = builder.length();
        if (length == 0) {
            return builder.toString();
        }
        return builder.substring(0, length - 1);
    }

    /**
     * 获取类型的全限定类名，数组在类名后加[]
     *
     * @param clazz 类型
     * @return 全限定类名
     */
    public static String getClassName(Class<?> clazz) {
        if (!clazz.isArray()) {
            return clazz.getName();
        }
        StringBuilder suffix = new StringBuilder();
        while (clazz.isArray()) {
            suffix.append("[]");
            clazz = clazz.getComponentType();
        }
        return clazz.getName() + suffix;
    }

    public static List<Class<?>> fromClassNames(String classNames) {
        if (classNames == null || StringUtils.isBlank(classNames) || classNames.equals("void")) {
            return Collections.emptyList();
        }
        return Arrays.stream(JRpcURLParamType.commaSplitPattern.getPattern().split(classNames))
                .map(ReflectUtils::fromClassName).collect(Collectors.toList());
    }

    public static Class<?> fromClassName(String className) {
        return NAME_CLASS_MAP.computeIfAbsent(className, c -> {
            try {
                return fromClassNameNoCache(className);
            } catch (ClassNotFoundException e) {
                log.error("cannot found class with mame {}.", className, e);
                return null;
            }
        });
    }

    public static Class<?> fromClassNameNoCache(String className) throws ClassNotFoundException {
        if (!className.endsWith("[]")) {
            Class<?> clazz = NAME_CLASS_MAP.get(className);
            return clazz == null ? Class.forName(className, true, Thread.currentThread().getContextClassLoader())
                    : clazz;
        }
        int dimension = 0;
        while (className.endsWith("[]")) {
            dimension++;
            className = className.substring(0, className.length() - 2);
        }
        int[] dimensions = new int[dimension];
        Class<?> clazz = NAME_CLASS_MAP.get(className);
        if (clazz == null) {
            clazz = Class.forName(className, true, Thread.currentThread().getContextClassLoader());
        }
        return Array.newInstance(clazz, dimensions).getClass();
    }
}
