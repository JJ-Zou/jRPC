package com.zjj.common.event;

import com.zjj.common.lang.Prioritized;
import com.zjj.common.utils.ReflectUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

@FunctionalInterface
public interface EventListener<E extends Event> extends java.util.EventListener, Prioritized {

    void onEvent(E event);

    @Override
    default int getPriority() {
        return NORMAL_PRIORITY;
    }

    static Class<? extends Event> findEventType(EventListener<?> listener) {
        return findEventType(listener.getClass());
    }

    static Class<? extends Event> findEventType(Class<?> listenerClass) {
        Class<? extends Event> eventType = null;
        if (listenerClass != null && EventListener.class.isAssignableFrom(listenerClass)) {
            eventType = ReflectUtils.findParameterizedTypes(listenerClass)
                    .stream()
                    .map(EventListener::findEventType)
                    .filter(Objects::nonNull)
                    .findAny()
                    .orElse(findEventType(listenerClass.getSuperclass()));
        }
        return eventType;
    }

    static Class<? extends Event> findEventType(ParameterizedType parameterizedType) {
        Class<? extends Event> eventType = null;
        Type rawType = parameterizedType.getRawType();
        if ((rawType instanceof Class) && EventListener.class.isAssignableFrom((Class) rawType)) {
            Type[] typeArguments = parameterizedType.getActualTypeArguments();
            for (Type typeArgument : typeArguments) {
                if (typeArgument instanceof Class) {
                    Class arguementClass = (Class) typeArgument;
                    if (Event.class.isAssignableFrom(arguementClass)) {
                        eventType = arguementClass;
                        break;
                    }
                }
            }
        }
        return eventType;
    }
}
