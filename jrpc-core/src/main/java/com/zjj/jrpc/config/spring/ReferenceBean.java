package com.zjj.jrpc.config.spring;

import com.zjj.jrpc.config.ReferenceConfig;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;

public class ReferenceBean<T> extends ReferenceConfig<T> implements FactoryBean<T>, DisposableBean {
    private static final long serialVersionUID = 1056107339201395653L;

    @Override
    public T getObject() throws Exception {
        return getRef();
    }

    @Override
    public Class<?> getObjectType() {
        return getInterfaceClass();
    }

    @Override
    public void destroy() throws Exception {
    }
}
