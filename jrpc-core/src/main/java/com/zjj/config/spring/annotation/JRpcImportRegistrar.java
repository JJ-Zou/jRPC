package com.zjj.config.spring.annotation;

import com.zjj.config.spring.beans.JRpcInstantiationAwareBeanPostProcessor;
import com.zjj.config.spring.beans.definition.JRpcServiceBeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.NonNull;

public class JRpcImportRegistrar implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(@NonNull AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        AbstractBeanDefinition registryPostProcessor = BeanDefinitionBuilder.rootBeanDefinition(JRpcServiceBeanDefinitionRegistryPostProcessor.class)
                .setRole(BeanDefinition.ROLE_INFRASTRUCTURE)
                .getBeanDefinition();
        registry.registerBeanDefinition(JRpcServiceBeanDefinitionRegistryPostProcessor.class.getSimpleName(), registryPostProcessor);

        AbstractBeanDefinition beanPostProcessor = BeanDefinitionBuilder.rootBeanDefinition(JRpcInstantiationAwareBeanPostProcessor.class)
                .setRole(BeanDefinition.ROLE_INFRASTRUCTURE)
                .getBeanDefinition();
        registry.registerBeanDefinition(JRpcInstantiationAwareBeanPostProcessor.class.getSimpleName(), beanPostProcessor);

    }
}
