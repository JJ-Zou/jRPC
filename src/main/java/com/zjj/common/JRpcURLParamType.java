package com.zjj.common;

public enum JRpcURLParamType {
    version("version", "1.0"),
    group("group", "default_rpc"),
    application("application", "jrpc"),
    module("module", "jrpc"),
    nodeType("nodeType", "service"),
    registryRetryPeriod("registryRetryPeriod", 30 * 1000),
    connectTimeout("connectTimeout", 1000),
    registrySessionTimeout("registrySessionTimeout", 60000),
    registryRetryTimes("registryRetryTimes", 1),
    sleepMsBetweenRetries("sleepMsBetweenRetries", 1000)
    ;
    private String name;
    private String value;
    private int intValue;

    JRpcURLParamType(String name, String value) {
        this.name = name;
        this.value = value;
    }

    JRpcURLParamType(String name, int intValue) {
        this.name = name;
        this.intValue = intValue;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public int getIntValue() {
        return intValue;
    }
}
