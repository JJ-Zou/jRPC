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
    retries("retries", 0),
    requestIdFromClient("requestIdFromClient", 0),
    heartBeatPeriod("heartBeatPeriod", 500),
    requestFlag("requestFlag", (byte) 0b00000000),
    responseFlag("responseFlag", (byte) 0b00000001),
    responseVoid("responseVoid", (byte) 0b00000011),
    responseException("responseException", (byte) 0b00000101),
    flagMask("flagMask", (byte) 0b00000111),
    magicNum("magicNum", (short) 0xabcd),
    host("host", ""),
    ;
    private String name;
    private String value;
    private byte byteValue;
    private short shortValue;
    private int intValue;
    private boolean booleanValue;

    JRpcURLParamType(String name, String value) {
        this.name = name;
        this.value = value;
    }

    JRpcURLParamType(String name, byte byteValue) {
        this.name = name;
        this.byteValue = byteValue;
    }

    JRpcURLParamType(String name, short shortValue) {
        this.name = name;
        this.shortValue = shortValue;
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

    public byte getByteValue() {
        return byteValue;
    }

    public short getShortValue() {
        return shortValue;
    }

    public boolean isBooleanValue() {
        return booleanValue;
    }
}
