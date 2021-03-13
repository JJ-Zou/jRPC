package com.zjj.rpc.message;

import com.zjj.protocol.ProtocolVersion;
import com.zjj.rpc.Response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefaultResponse implements Response, Serializable {
    private static final long serialVersionUID = -6065953058349298203L;

    private Object value;
    private Exception exception;
    private long requestId;
    private long processTime;
    private int timeout;
    private Map<String, String> attachments;
    private byte protocolVersion = ProtocolVersion.DEFAULT_VERSION.getVersion();
    private int serializeNumber = 0;

    public DefaultResponse(Object value) {
        this.value = value;
    }

    public DefaultResponse(Response response) {
        this.value = response.getValue();
        this.exception = response.getException();
        this.requestId = response.getRequestId();
        this.processTime = response.getProcessTime();
        this.timeout = response.getTimeout();
        this.attachments = new HashMap<>(response.getAttachments());
        this.protocolVersion = response.getProtocolVersion();
        this.serializeNumber = response.getSerializeNumber();
    }

    @Override
    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    @Override
    public long getRequestId() {
        return requestId;
    }

    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }

    @Override
    public long getProcessTime() {
        return processTime;
    }

    @Override
    public void setProcessTime(long processTime) {
        this.processTime = processTime;
    }

    @Override
    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public Map<String, String> getAttachments() {
        return attachments == null ? Collections.emptyMap() : Collections.unmodifiableMap(attachments);
    }

    public void setAttachments(Map<String, String> attachments) {
        this.attachments = new HashMap<>(attachments);
    }

    @Override
    public void setAttachment(String key, String value) {
        if (attachments == null) {
            attachments = new HashMap<>();
        }
        attachments.put(key, value);
    }

    @Override
    public boolean containsAttachment(String key) {
        if (attachments == null) {
            return false;
        }
        return attachments.containsKey(key);
    }

    @Override
    public String getAttachment(String key) {
        return attachments == null ? null : attachments.get(key);
    }

    @Override
    public byte getProtocolVersion() {
        return protocolVersion;
    }

    @Override
    public void setProtocolVersion(byte protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    @Override
    public int getSerializeNumber() {
        return serializeNumber;
    }

    @Override
    public void setSerializeNumber(int serializeNumber) {
        this.serializeNumber = serializeNumber;
    }

    @Override
    public String toString() {
        return "DefaultResponse{" +
                "value=" + value +
                ", exception=" + exception +
                ", requestId=" + requestId +
                ", processTime=" + processTime +
                ", timeout=" + timeout +
                ", attachments=" + attachments +
                ", protocolVersion=" + protocolVersion +
                ", serializeNumber=" + serializeNumber +
                '}';
    }
}
