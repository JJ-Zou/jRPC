package com.zjj.jrpc.common;

import java.util.regex.Pattern;

public enum JRpcURLParamType {
    VERSION("version", "1.0"),
    GROUP("group", "default_rpc"),
    APPLICATION("application", "jrpc"),
    MODULE("module", "jrpc"),
    NODE_TYPE("nodeType", "service"),
    REFERER("referer", "referer"),
    REGISTRY_RETRY_PERIOD("registryRetryPeriod", 30 * 1000),
    CONNECT_TIMEOUT("connectTimeout", 5000),
    REGISTRY_SESSION_TIMEOUT("registrySessionTimeout", 60000),
    REGISTRY_RETRY_TIMES("registryRetryTimes", 1),
    SLEEP_MS_BETWEEN_RETRIES("sleepMsBetweenRetries", 1000),
    MAX_SERVER_CONNECTION("maxServerConnection", 100000),
    WORKER_QUEUE_SIZE("workerQueueSize", 0),
    CORE_POOL_SIZE("corePoolSize", 20),
    MAXIMUM_POOL_SIZE("maximumPoolSize", 200),
    CLIENT_INIT_CONNECTIONS("clientInitConnections", 2),
    IS_ASYNC_INIT_CONNECTIONS("isAsyncInitConnections", false),
    CONNECT_TIMEOUT_MILLS("connectTimeoutMills", 5000),
    RETRIES("retries", 0),
    REQUEST_ID_FROM_CLIENT("requestIdFromClient", 0),
    HEART_BEAT_PERIOD("heartBeatPeriod", 500),
    REQUEST_FLAG("requestFlag", (byte) 0b00000000),
    RESPONSE_FLAG("responseFlag", (byte) 0b00000001),
    RESPONSE_VOID("responseVoid", (byte) 0b00000011),
    RESPONSE_EXCEPTION("responseException", (byte) 0b00000101),
    MAGIC_NUM("magicNum", (short) 0xabcd),
    HOST("host", ""),
    NETTY_MAGIC_NUM("nettyMagicNum", (short) 0xdcba),
    REQUEST_TIMEOUT("requestTimeout", 200),
    THROW_EXCEPTION("throwException", true),
    COMMA_SPLIT_PATTERN("registrySplitPattern", Pattern.compile("\\s*[,]\\s*")),
    EQUAL_SPLIT_PATTERN("equalSplitPattern", Pattern.compile("\\s*[=]\\s*")),
    COLON("colon", ":"),
    COLON_SPLIT_PATTERN("colonSplitPattern", Pattern.compile("\\s*[:]\\s*")),
    PROTOCOL("protocol", "jrpc"),
    LOCALHOST("localhost", "127.0.0.1"),
    DIRECT_CONNECT_URL("directConnectUrl", ""),
    REFRESH_TIMESTAMP("refreshTimestamp", 0),
    PERIOD("period ", "."),
    METHOD_CONFIG_PREFIX("method_config_prefix ", "methodConfig."),
    DEFAULT_CLASS("defaultClass", void.class),
    EXPORT_PROTOCOL("exportProtocol", "jrpc:32121"),
    EXPORT_REGISTRY("registry", "registry"),
    EXPORT_PORT("exportPort", 32121),
    EXPORT("export", true),
    EXPORT_HOST("exportHost", "127.0.0.1"),
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
