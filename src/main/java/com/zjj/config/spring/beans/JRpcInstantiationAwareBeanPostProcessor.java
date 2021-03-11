package com.zjj.config.spring.beans;

import com.zjj.config.spring.ServiceBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.lang.NonNull;

@Slf4j
public class JRpcInstantiationAwareBeanPostProcessor implements InstantiationAwareBeanPostProcessor {

    @Override
    public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
        if (beanClass == ServiceBean.class) {
            log.error("[postProcessBeforeInstantiation] beanClass: {}, beanName: {}", beanClass, beanName);
        }
        return null;
    }

    @Override
    public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
        if (bean.getClass() == ServiceBean.class) {
            log.error("[postProcessAfterInstantiation] bean: {}, beanName: {}", bean, beanName);
        }
        return true;
    }

    @Override
    public PropertyValues postProcessProperties(@NonNull PropertyValues pvs, Object bean, @NonNull String beanName) throws BeansException {
        if (bean.getClass() == ServiceBean.class) {
            log.error("[postProcessProperties] pvs: {}, bean: {}, beanName: {}", pvs, bean, beanName);
        }
        return pvs;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass() == ServiceBean.class) {
            log.error("[postProcessBeforeInitialization] bean: {}, beanName: {}", bean, beanName);
        }
        return null;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass() == ServiceBean.class) {
            System.out.println(((ServiceBean<?>) bean).getInterfaceClass());
            log.error("[postProcessAfterInitialization] bean: {}, beanName: {}", bean, beanName);
        }
        return null;
    }
}
