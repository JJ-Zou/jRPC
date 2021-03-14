package com.zjj.jrpc.rpc.message;

import com.zjj.jrpc.protocol.ProtocolVersion;
import com.zjj.jrpc.rpc.Request;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DefaultRequest implements Request, Serializable {
    private static final long serialVersionUID = -350002615558853010L;

    private String interfaceName;
    private String methodName;
    private String parameterSign;
    private Object[] arguments;
    private Map<String, String> attachments;
    private int retries;
    private long requestId;
    private byte protocolVersion = ProtocolVersion.DEFAULT_VERSION.getVersion();
    private int serializeNumber = 0;


    @Override
    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    @Override
    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    @Override
    public String getParameterSign() {
        return parameterSign;
    }

    public void setParameterSign(String parameterSign) {
        this.parameterSign = parameterSign;
    }

    @Override
    public Object[] getArguments() {
        return arguments == null ? new Object[0] : arguments;
    }

    public void setArguments(Object[] arguments) {
        this.arguments = arguments;
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
    public long getRequestId() {
        return requestId;
    }

    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }

    @Override
    public int getRetries() {
        return retries;
    }

    @Override
    public void setRetries(int retries) {
        this.retries = retries;
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
        return "DefaultRequest{" +
                "interfaceName='" + interfaceName + '\'' +
                ", methodName='" + methodName + '\'' +
                ", parameterSign='" + parameterSign + '\'' +
                ", arguments=" + Arrays.toString(arguments) +
                ", attachments=" + attachments +
                ", retries=" + retries +
                ", requestId=" + requestId +
                ", protocolVersion=" + protocolVersion +
                ", serializeNumber=" + serializeNumber +
                '}';
    }
}
