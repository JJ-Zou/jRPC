package com.zjj.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

@Setter
@Getter
@Slf4j
public abstract class AbstractConfig implements Serializable {

    private static final long serialVersionUID = -7055165458905230077L;

    protected String id;


    private boolean isGetter(Method method) {
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

    private static final String[] SUFFIXES = new String[]{"Config", "Bean"};

    private static String getTagName(Class<?> cls) {
        String tag = cls.getSimpleName();
        for (String suffix : SUFFIXES) {
            if (tag.endsWith(suffix)) {
                tag = tag.substring(0, tag.length() - suffix.length());
                break;
            }
        }
        return tag.toLowerCase();
    }

    private static String calculateAttributeFromGetter(String getter) {
        int i = getter.startsWith("get") ? 3 : 2;
        return getter.substring(i, i + 1).toLowerCase() + getter.substring(i + 1);
    }

    @Override
    public String toString() {
        try {
            StringBuilder builder = new StringBuilder();
            builder.append("<jrpc:")
                    .append(getTagName(getClass()));
            for (Method method : getClass().getMethods()) {
                if (!isGetter(method)) {
                    continue;
                }
                try {
                    String filed = calculateAttributeFromGetter(method.getName());
                    Object value = method.invoke(this);
                    if (value != null) {
                        builder.append(" ").append(filed).append("=\"").append(value).append("\"");
                    }
                } catch (Exception e) {
                    log.warn(e.getMessage(), e);
                }
            }
            builder.append(" />");
            return builder.toString();
        } catch (Throwable e) {
            log.warn(e.getMessage(), e);
            return super.toString();
        }
    }
}
