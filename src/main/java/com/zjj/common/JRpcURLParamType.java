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
    sleepMsBetweenRetries("sleepMsBetweenRetries", 1000),
    maxServerConnection("maxServerConnection", 100000),
    workerQueueSize("workerQueueSize", 0),
    corePoolSize("corePoolSize", 20),
    maximumPoolSize("maximumPoolSize", 200),
    clientInitConnections("clientInitConnections", 2),
    isAsyncInitConnections("isAsyncInitConnections", false),
    connectTimeoutMills("connectTimeoutMills", 5000),
    ;
    private String name;
    private String value;
    private int intValue;
    private boolean booleanValue;

    JRpcURLParamType(String name, String value) {
        this.name = name;
        this.value = value;
    }

    JRpcURLParamType(String name, int intValue) {
        this.name = name;
        this.intValue = intValue;
    }

    JRpcURLParamType(String name, boolean booleanValue) {
        this.name = name;
        this.booleanValue = booleanValue;
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

    public boolean isBooleanValue() {
        return booleanValue;
    }
}
