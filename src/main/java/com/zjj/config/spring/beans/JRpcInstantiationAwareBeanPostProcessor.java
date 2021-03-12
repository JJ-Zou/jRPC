package com.zjj.config.spring.beans;

import com.zjj.config.AbstractConfig;
import com.zjj.config.support.ConfigBeanManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.lang.NonNull;

@Slf4j
public class JRpcInstantiationAwareBeanPostProcessor implements InstantiationAwareBeanPostProcessor {

    @Override
    public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
        if (AbstractConfig.class.isAssignableFrom(beanClass)) {
            log.info("[postProcessBeforeInstantiation] beanClass: {}, beanName: {}", beanClass, beanName);
        }
        return null;
    }

    @Override
    public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
        if (AbstractConfig.class.isAssignableFrom(bean.getClass())) {
            log.info("[postProcessAfterInstantiation] bean: {}, beanName: {}", bean, beanName);
        }
        return true;
    }

    @Override
    public PropertyValues postProcessProperties(@NonNull PropertyValues pvs, Object bean, @NonNull String beanName) throws BeansException {
        if (AbstractConfig.class.isAssignableFrom(bean.getClass())) {
            ((MutablePropertyValues) pvs).add("id", beanName);
            log.info("[bean: {}, beanName: {}] propertyValues: {}", bean, beanName, pvs.getPropertyValues());
        }
        return null;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (AbstractConfig.class.isAssignableFrom(bean.getClass())) {
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (AbstractConfig.class.isAssignableFrom(bean.getClass())) {
            ConfigBeanManager.addAbstractConfig(beanName, (AbstractConfig) bean);
            log.info("put ({}, {}) into ConfigBeanManager success", beanName, bean);
        }
        return bean;
    }

}
