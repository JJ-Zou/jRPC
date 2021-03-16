package com.zjj.jrpc.common.utils;

import com.zjj.jrpc.common.JRpcURLParamType;
import com.zjj.jrpc.config.spring.ServiceBean;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.util.StringUtils;

import java.util.StringJoiner;

public class BeanNameUtils {
    private BeanNameUtils() {
    }

    public static String[] getExportProtocolBeanName(CharSequence exportProtocol) {
        return JRpcURLParamType.COMMA_SPLIT_PATTERN.getPattern().split(exportProtocol);

    }

    public static String[] getExportRegistryBeanName(CharSequence registry) {
        return JRpcURLParamType.COMMA_SPLIT_PATTERN.getPattern().split(registry);
    }

    public static String buildServiceBeanName(AnnotationAttributes annotationAttributes, Class<?> interfaceClass) {
        StringJoiner stringJoiner = new StringJoiner(JRpcURLParamType.COLON.getValue());
        return stringJoiner.add(ServiceBean.class.getSimpleName())
                .add(interfaceClass.getName())
                .add(annotationAttributes.getString(JRpcURLParamType.VERSION.getName()))
                .add(annotationAttributes.getString(JRpcURLParamType.GROUP.getName()))
                .toString();
    }

    public static String buildReferenceBeanName(AnnotationAttributes annotationAttributes, Class<?> interfaceClass) {
        String beanName = annotationAttributes.getString("id");
        if (!StringUtils.isEmpty(beanName)) {
            return beanName;
        }
        StringJoiner joiner = new StringJoiner(",");
        annotationAttributes.forEach((k, v) -> joiner.add(k + "=" + v));
        return "@JRpcReference(" + joiner.toString() + ") " + interfaceClass.getName();
    }

}
