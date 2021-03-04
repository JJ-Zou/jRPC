package com.zjj.rpc.config.annotation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

@Slf4j
public class JRpcComponentScanRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        Set<String> packagesToScan = getPackagesToScan(metadata);
        registerServiceAnnotationBeanPostProcessor(packagesToScan, registry);
    }

    private void registerServiceAnnotationBeanPostProcessor(Set<String> packagesToScan, BeanDefinitionRegistry registry) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(ServiceClassPostProcessor.class);
        builder.addConstructorArgValue(packagesToScan);
        builder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
        AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
        BeanDefinitionReaderUtils.registerWithGeneratedName(beanDefinition, registry);
    }

    /**
     * 从metadata中获取含注解@JRpcComponentScan的包
     *
     * @param metadata metadata
     * @return 被扫描包的集合
     */
    private Set<String> getPackagesToScan(AnnotationMetadata metadata) {
        AnnotationAttributes annotationAttributes = AnnotationAttributes
                .fromMap(metadata.getAnnotationAttributes(JRpcComponentScan.class.getName()));
        if (annotationAttributes == null) {
            throw new IllegalStateException("JRpcComponentScan package is null.");
        }
        String[] value = annotationAttributes.getStringArray("value");
        String[] basePackages = annotationAttributes.getStringArray("basePackages");
        Class<?>[] basePackageClasses = annotationAttributes.getClassArray("basePackageClasses");
        Set<String> packageToScan = new LinkedHashSet<>(Arrays.asList(value));
        packageToScan.addAll(Arrays.asList(basePackages));
        for (Class<?> basePackageClass : basePackageClasses) {
            packageToScan.add(ClassUtils.getPackageName(basePackageClass));
        }
        if (packageToScan.isEmpty()) {
            return Collections.singleton(ClassUtils.getPackageName(metadata.getClassName()));
        }
        return packageToScan;
    }

}
