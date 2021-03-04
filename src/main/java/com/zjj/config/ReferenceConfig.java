package com.zjj.config;

import java.util.List;

public class ReferenceConfig<T> extends AbstractReferenceConfig {
    private static final long serialVersionUID = -8529195602996641811L;

    private T ref;

    private Class<T> interfaceClass;

    private String serverInterface;

    protected List<MethodConfig> methods;

}
