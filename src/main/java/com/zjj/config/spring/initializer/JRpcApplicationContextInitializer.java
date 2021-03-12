package com.zjj.config.spring.initializer;

import com.zjj.config.spring.beans.JRpcBeanDefinitionRegistryPostProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

@Slf4j
public class JRpcApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        applicationContext.addBeanFactoryPostProcessor(new JRpcBeanDefinitionRegistryPostProcessor());
        log.error("applicationContext {}: {}", applicationContext.getClass().getSimpleName(), applicationContext);
    }
}
