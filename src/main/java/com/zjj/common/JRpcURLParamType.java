package com.zjj.common;

import java.util.regex.Pattern;

public enum JRpcURLParamType {
    version("version", "1.0"),
    group("group", "default_rpc"),
    application("application", "jrpc"),
    module("module", "jrpc"),
    nodeType("nodeType", "service"),
    referer("referer", "referer"),
    registryRetryPeriod("registryRetryPeriod", 30 * 1000),
    connectTimeout("connectTimeout", 5000),
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
    magicNum("magicNum", (short) 0xabcd),
    host("host", ""),
    nettyMagicNum("nettyMagicNum", (short) 0xdcba),
    requestTimeout("requestTimeout", 200),
    throwException("throwException", true),
    commaSplitPattern("registrySplitPattern", Pattern.compile("\\s*[,]\\s*")),
    equalSplitPattern("equalSplitPattern", Pattern.compile("\\s*[=]\\s*")),
    colon("colon", ":"),
    colonSplitPattern("colonSplitPattern", Pattern.compile("\\s*[:]\\s*")),
    protocol("protocol", "jrpc"),
    localhost("localhost", "127.0.0.1"),
    directConnectUrl("directConnectUrl", ""),
    refreshTimestamp("refreshTimestamp", 0),
    period("period ", "."),
    method_config_prefix("method_config_prefix ", "methodConfig."),
    defaultClass("defaultClass", void.class),
    exportProtocol("exportProtocol", "jrpc:32121"),
    exportRegistry("exportRegistry", "registryConfig"),
    exportPort("exportPort", 32121),
    export("export", true),
    exportHost("exportHost", "127.0.0.1"),
    ;
    private final String name;
    private String value;
    private byte byteValue;
    private short shortValue;
    private int intValue;
    private boolean booleanValue;
    private Pattern pattern;
    private Class<?> type;

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

    JRpcURLParamType(String name, Pattern pattern) {
        this.name = name;
        this.pattern = pattern;
    }

    JRpcURLParamType(String name, Class<?> type) {
        this.name = name;
        this.type = type;
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

    public Pattern getPattern() {
        return pattern;
    }

    public Class<?> getType() {
        return type;
    }
}
