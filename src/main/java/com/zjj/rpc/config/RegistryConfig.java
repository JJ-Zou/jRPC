package com.zjj.rpc.config;

import com.zjj.rpc.config.support.Parameter;

public class RegistryConfig extends AbstractConfig {
    private static final long serialVersionUID = 4149044787940606573L;
    public static final String BEAN_NAME = "registryConfig";

    private String protocol;
    private String address;
    private Integer port;

    public RegistryConfig() {
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    @Parameter(excluded = true)
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    @Override
    @Parameter(excluded = true)
    public String getBeanName() {
        return BEAN_NAME;
    }

}
