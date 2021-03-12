package com.zjj.config;

import com.zjj.common.JRpcURLParamType;
import com.zjj.common.utils.ReflectUtils;
import com.zjj.exception.JRpcErrorMessage;
import com.zjj.exception.JRpcFrameworkException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

    public String getTagName() {
        String tag = this.getClass().getSimpleName();
        for (String suffix : SUFFIXES) {
            if (tag.endsWith(suffix)) {
                tag = tag.substring(0, tag.length() - suffix.length());
                break;
            }
        }
        return tag.toLowerCase();
    }

    public static String getTagName(Class<?> clazz) {
        String tag = clazz.getSimpleName();
        for (String suffix : SUFFIXES) {
            if (tag.endsWith(suffix)) {
                tag = tag.substring(0, tag.length() - suffix.length());
                break;
            }
        }
        return tag.toLowerCase();
    }

    protected void refreshConfigs(Map<String, String> params, AbstractConfig... abstractConfigs) {
        Arrays.stream(abstractConfigs).forEach(abstractConfig -> abstractConfig.appendConfigParams(params, null));
    }

    protected void appendConfigParams(Map<String, String> params, String prefix) {
        Method[] methods = getClass().getMethods();
        Arrays.stream(methods)
                .forEach(method -> {
                    if (ReflectUtils.isConfigGetter(method)) {
                        String filedProperty = ReflectUtils.getPropertyFromGetter(method);
                        filedProperty = (prefix == null) ? filedProperty : (prefix + JRpcURLParamType.period.getValue() + filedProperty);
                        Object value = null;
                        try {
                            value = method.invoke(this);
                            if (value != null) {
                                params.put(filedProperty, String.valueOf(value));
                            }
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            log.error("filed property {} cannot obtain.", filedProperty);
                        }
                    } else if (method.getName().equals("getParameters") && ReflectUtils.isMapGetter(method)) {
                        Map<String, String> map;
                        try {
                            map = (Map<String, String>) method.invoke(this);
                            params.putAll(map);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            log.error("Map<String, String> parameters property cannot obtain.");
                        }
                    }
                });

    }

    protected void refreshMethodConfigs(Map<String, String> param, List<MethodConfig> methodConfigs) {
        if (CollectionUtils.isEmpty(methodConfigs)) {
            return;
        }
        methodConfigs.forEach(methodConfig ->
                methodConfig.appendConfigParams(param,
                        JRpcURLParamType.method_config_prefix.getValue()
                                + methodConfig.getName()
                                + "("
                                + methodConfig.getArgumentTypes()
                                + ")"));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("<jrpc:")
                .append(getTagName());
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
