package com.zjj.jrpc.common.utils;

import com.zjj.jrpc.config.spring.annotation.JRpcComponent;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.core.type.AnnotationMetadata;

public class JRpcComponentUtils {
    private JRpcComponentUtils() {
    }

    public static boolean checkJRpcComponentScanCandidate(BeanDefinition beanDef) {
        String className = beanDef.getBeanClassName();
        if (className == null) {
            return false;
        }
        if (!(beanDef instanceof AnnotatedBeanDefinition) ||
                !className.equals(((AnnotatedBeanDefinition) beanDef).getMetadata().getClassName())) {
            return false;
        }
        AnnotationMetadata metadata = ((AnnotatedBeanDefinition) beanDef).getMetadata();
        return isJRpcComponentScanCandidate(metadata);
    }

    public static boolean isJRpcComponentScanCandidate(AnnotationMetadata metadata) {
        if (metadata.isInterface()) {
            return false;
        }
        return metadata.isAnnotated(JRpcComponent.class.getName());
    }
}
