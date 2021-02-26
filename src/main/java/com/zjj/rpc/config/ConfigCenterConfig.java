package com.zjj.rpc.config;

import com.zjj.rpc.common.JRpcURL;
import com.zjj.rpc.config.support.Parameter;

import java.util.concurrent.atomic.AtomicBoolean;

public class ConfigCenterConfig extends AbstractConfig {
    private static final long serialVersionUID = 5273895441172778616L;
    public static final String BEAN_NAME = "configCenterConfig";
    private AtomicBoolean inited = new AtomicBoolean(false);

    private String protocol;
    private String address;
    private Integer port;

    public ConfigCenterConfig() {
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

    public boolean checkOrUpdateInited() {
        return inited.compareAndSet(false, true);
    }

    public JRpcURL toUrl() {
        return new JRpcURL(getProtocol(), getAddress(), getPort(), getClass().getSimpleName());
    }

}
