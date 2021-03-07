package com.zjj.rpc;

import java.util.Map;

public interface Response {
    Object getValue();

    Exception getException();

    long getRequestId();

    long getProcessTime();

    void setProcessTime(long processTime);

    int getTimeout();

    Map<String, String> getAttachments();

    void setAttachment(String key, String value);

    boolean containsAttachment(String key);

    String getAttachment(String key);

    byte getProtocolVersion();

    void setProtocolVersion(byte protocolVersion);

    int getSerializeNumber();

    void setSerializeNumber(int number);

}
