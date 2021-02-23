package com.zjj.common.event;


import com.zjj.common.function.ThrowableConsumer;
import com.zjj.common.function.ThrowableFunction;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Stream;

public abstract class GenericEventListener implements EventListener<Event> {
    private final Method onEventMethod;

    private final Map<Class<?>, Set<Method>> handleEventMethods;

    public GenericEventListener() {
        this.onEventMethod = findOnEventMethod();
        this.handleEventMethods = findHandlerEventMethods();
    }

    private Method findOnEventMethod() {
        return ThrowableFunction.execute(getClass(),
                listenerClass -> listenerClass.getMethod("onEvent", Event.class)
        );
    }

    private Map<Class<?>, Set<Method>> findHandlerEventMethods() {
        Map<Class<?>, Set<Method>> eventMethods = new HashMap<>();
        Stream.of(getClass().getMethods())
                .filter(this::isHandlerEventMethod)
                .forEach(method -> {
                    Class<?> parameterType = method.getParameterTypes()[0];
                    Set<Method> methods = eventMethods.computeIfAbsent(parameterType, key -> new LinkedHashSet<>());
                    methods.add(method);
                });
        return eventMethods;
    }

    private boolean isHandlerEventMethod(Method method) {
        if (onEventMethod.equals(method)) {
            return false;
        }
        if (!Modifier.isPublic(method.getModifiers())) {
            return false;
        }
        if (!void.class.equals(method.getReturnType())) {
            return false;
        }
        Class<?>[] exceptionTypes = method.getExceptionTypes();
        if (exceptionTypes.length > 0) {
            return false;
        }
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length != 1) {
            return false;
        }
        if (!Event.class.isAssignableFrom(parameterTypes[0])) {
            return false;
        }
        return true;
    }

    @Override
    public void onEvent(Event event) {
        Class<? extends Event> eventClass = event.getClass();
        handleEventMethods.getOrDefault(eventClass, Collections.emptySet())
                .forEach(method -> ThrowableConsumer.execute(method, m -> m.invoke(this, event)));
    }
}
