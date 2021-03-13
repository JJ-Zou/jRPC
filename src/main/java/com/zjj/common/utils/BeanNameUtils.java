package com.zjj.common.utils;

import com.zjj.common.JRpcURLParamType;
import com.zjj.config.spring.ServiceBean;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.StringJoiner;

public class BeanNameUtils {
    private BeanNameUtils() {
    }

    public static String[] getExportProtocolBeanName(CharSequence exportProtocol) {
        return Arrays.stream(JRpcURLParamType.commaSplitPattern.getPattern().split(exportProtocol))
                .map(str -> JRpcURLParamType.colonSplitPattern.getPattern().split(str)[0])
                .toArray(String[]::new);
    }

    public static String[] getExportRegistryBeanName(CharSequence registry) {
        return JRpcURLParamType.commaSplitPattern.getPattern().split(registry);
    }

    public static String buildServiceBeanName(AnnotationAttributes annotationAttributes, Class<?> interfaceClass) {
        StringJoiner stringJoiner = new StringJoiner(JRpcURLParamType.colon.getValue());
        return stringJoiner.add(ServiceBean.class.getSimpleName())
                .add(interfaceClass.getName())
                .add(annotationAttributes.getString(JRpcURLParamType.version.getName()))
                .add(annotationAttributes.getString(JRpcURLParamType.group.getName()))
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
