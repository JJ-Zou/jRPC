package com.zjj.rpc;

import com.zjj.common.JRpcURLParamType;

import java.util.Map;

public interface Request {
    String getInterfaceName();

    String getMethodName();

    String getParameterSign();

    Object[] getArguments();

    Map<String, String> getAttachments();

    void setAttachment(String key, String value);

    boolean containsAttachment(String key);

    String getAttachment(String key);

    long getRequestId();

    int getRetries();

    void setRetries(int retries);

    byte getProtocolVersion();

    void setProtocolVersion(byte protocolVersion);

    int getSerializeNumber();

    void setSerializeNumber(int number);

    default String getServiceKey() {
        String version = getOrDefault(JRpcURLParamType.version.getName(), JRpcURLParamType.version.getValue());
        String group = getOrDefault(JRpcURLParamType.group.getName(), JRpcURLParamType.group.getValue());
        return group + "/" + getInterfaceName() + "/" + version;
    }

    default String getOrDefault(String key, String defaultValue) {
        return containsAttachment(key) ? getAttachment(key) : defaultValue;
    }

}
