package com.zjj.config.spring.context;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;

import java.util.Objects;

abstract class OneTimeExecutionApplicationContextEventListener implements ApplicationListener, ApplicationContextAware {
    private ApplicationContext applicationContext;

    @Override
    public final void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (isOriginalEventSource(event) && event instanceof ApplicationContextEvent) {
            onApplicationContextEvent((ApplicationContextEvent) event);
        }
    }

    protected abstract void onApplicationContextEvent(ApplicationContextEvent event);

    private boolean isOriginalEventSource(ApplicationEvent event) {
        return applicationContext == null || Objects.equals(applicationContext, event.getSource());
    }
}
