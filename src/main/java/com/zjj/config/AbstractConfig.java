package com.zjj.config;

import com.zjj.common.utils.ReflectUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
public abstract class AbstractConfig implements Serializable {

    private static final long serialVersionUID = -7055165458905230077L;

    protected String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("<jrpc:")
                .append(getTagName(getClass()));
        for (Method method : getClass().getMethods()) {
            if (!ReflectUtils.isGetter(method)) {
                continue;
            }
            try {
                String filed = ReflectUtils.getPropertyFromGetter(method);
                Object value = method.invoke(this);
                if (value != null) {
                    builder.append(" ").append(filed).append("=\"").append(value).append("\"");
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                log.warn(e.getMessage(), e);
            }
        }
        builder.append(" />");
        try {
            return builder.toString();
        } catch (Exception e) {
            return super.toString();
        }
    }
}
