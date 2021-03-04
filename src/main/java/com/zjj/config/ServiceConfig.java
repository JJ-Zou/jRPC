package com.zjj.config;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceConfig<T> extends AbstractServiceConfig {
    private static final long serialVersionUID = 7509808393399061064L;

    private static final Set<String> ACTIVE_SERVICES = ConcurrentHashMap.newKeySet();

    protected List<MethodConfig> methods;

    private T ref;

    private Class<?> interfaceClass;

}
