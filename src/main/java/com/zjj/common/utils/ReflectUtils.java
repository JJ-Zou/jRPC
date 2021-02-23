package com.zjj.common.utils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class ReflectUtils {
    public static Set<ParameterizedType> findParameterizedTypes(Class<?> sourceClass) {
        List<Type> genericTypes = new LinkedList<>(Arrays.asList(sourceClass.getGenericInterfaces()));
        genericTypes.add(sourceClass.getGenericSuperclass());
        Set<ParameterizedType> parameterizedTypes = genericTypes.stream()
                .filter(type -> type instanceof ParameterizedType)
                .map(type -> ParameterizedType.class.cast(type))
                .collect(Collectors.toSet());
        if (parameterizedTypes.isEmpty()) {
            genericTypes.stream()
                    .filter(type -> type instanceof Class)
                    .map(type -> Class.class.cast(type))
                    .forEach(superClass -> {
                        parameterizedTypes.addAll(findParameterizedTypes(superClass));
                    });
        }
        return Collections.unmodifiableSet(parameterizedTypes);
    }

    public static boolean isPrimitives(Class<?> cls) {
        if (cls.isArray()) {
            return isPrimitive(cls.getComponentType());
        }
        return isPrimitive(cls);
    }

    public static boolean isPrimitive(Class<?> cls) {
        return cls.isPrimitive() || cls == String.class || cls == Boolean.class
                || cls == Character.class || Number.class.isAssignableFrom(cls) || Date.class.isAssignableFrom(cls);
    }
}
