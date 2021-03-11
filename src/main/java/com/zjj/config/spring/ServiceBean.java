package com.zjj.config.spring;

import com.zjj.config.ServiceConfig;

public class ServiceBean<T> extends ServiceConfig<T> {
    private static final long serialVersionUID = -5173125703434531081L;

    private transient String beanName;

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }
}
