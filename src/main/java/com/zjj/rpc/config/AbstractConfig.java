package com.zjj.rpc.config;

import com.zjj.rpc.common.urils.ReflectUtils;
import com.zjj.rpc.common.urils.StringUtils;
import com.zjj.rpc.config.context.ConfigManager;
import com.zjj.rpc.config.support.Parameter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Objects;

@Slf4j
public abstract class AbstractConfig implements Serializable {
    private static final long serialVersionUID = -6602445245369327147L;

    private static final String[] SUFFIXES = new String[]{"Config", "Bean", "ConfigBase"};

    protected String id;
    protected String prefix;

    @Parameter(excluded = true)
    public static String getTagName(Class<?> cls) {
        String tag = cls.getSimpleName();
        for (String suffix : SUFFIXES) {
            if (tag.endsWith(suffix)) {
                tag = tag.substring(0, tag.length() - suffix.length());
                break;
            }
        }
        return StringUtils.camelToSplitName(tag, "-");
    }

    @Parameter(excluded = true)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Parameter(excluded = true)
    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Parameter(excluded = true)
    public abstract String getBeanName();

    @PostConstruct
    public void addIntoConfigManager() {
        ConfigManager.addConfig(this);
    }

    @Override
    public int hashCode() {
        int hashCode = 1;

        Method[] methods = this.getClass().getMethods();
        for (Method method : methods) {
            if (ReflectUtils.isGetter(method)) {
                Parameter parameter = method.getAnnotation(Parameter.class);
                if (parameter != null && parameter.excluded()) {
                    continue;
                }
                try {
                    Object value = method.invoke(this, new Object[]{});
                    hashCode = 31 * hashCode + value.hashCode();
                } catch (Exception ignored) {
                }
            }
        }
        if (hashCode == 0) {
            hashCode = 1;
        }
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj != null && Objects.equals(obj.getClass().getName(), this.getClass().getName()))) {
            return false;
        }

        Method[] methods = this.getClass().getMethods();
        for (Method method1 : methods) {
            if (ReflectUtils.isGetter(method1)) {
                Parameter parameter = method1.getAnnotation(Parameter.class);
                if (parameter != null && parameter.excluded()) {
                    continue;
                }
                try {
                    Method method2 = obj.getClass().getMethod(method1.getName(), method1.getParameterTypes());
                    Object value1 = method1.invoke(this, new Object[]{});
                    Object value2 = method2.invoke(obj, new Object[]{});
                    if (!Objects.equals(value1, value2)) {
                        return false;
                    }
                } catch (Exception e) {
                    return true;
                }
            }
        }
        return true;
    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("<rpc:").append(getTagName(getClass()));
        Method[] methods = getClass().getMethods();
        for (Method method : methods) {
            if (ReflectUtils.isGetter(method)) {
                String name = method.getName();
                String param = StringUtils.calculateAttributeFromGetter(name);
                try {
                    getClass().getDeclaredField(param);
                } catch (NoSuchFieldException e) {
                    continue;
                }
                try {
                    Object value = method.invoke(this);
                    if (value != null) {
                        builder.append(" ").append(param).append("=\"").append(value).append("\"");
                    }
                } catch (Exception t) {
                    log.warn(t.getMessage(), t);
                }
            }
        }
        builder.append(" />");
        return builder.toString();
    }

}
