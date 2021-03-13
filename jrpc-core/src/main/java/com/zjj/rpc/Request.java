package com.zjj.rpc;

import com.zjj.common.JRpcURLParamType;

import java.util.Map;

public interface Request extends Message{
    String getInterfaceName();

    String getMethodName();

    String getParameterSign();

    Object[] getArguments();

    Map<String, String> getAttachments();

    void setAttachment(String key, String value);

    boolean containsAttachment(String key);

    String getAttachment(String key);



    int getRetries();

    void setRetries(int retries);

    byte getProtocolVersion();

    void setProtocolVersion(byte protocolVersion);

    int getSerializeNumber();

    void setSerializeNumber(int number);

    default String getServiceKey() {
        String version = getOrDefault(JRpcURLParamType.VERSION.getName(), JRpcURLParamType.VERSION.getValue());
        String group = getOrDefault(JRpcURLParamType.GROUP.getName(), JRpcURLParamType.GROUP.getValue());
        return group + "/" + getInterfaceName() + "/" + version;
    }

    default String getOrDefault(String key, String defaultValue) {
        return containsAttachment(key) ? getAttachment(key) : defaultValue;
    }

}
