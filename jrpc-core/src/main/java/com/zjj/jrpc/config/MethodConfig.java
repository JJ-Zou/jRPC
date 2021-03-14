package com.zjj.jrpc.config;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MethodConfig extends AbstractConfig {
    private static final long serialVersionUID = -8264818910565275440L;
    private String name;
    private Integer requestTimeout;
    private Integer retries;
    private String argumentTypes;

}
