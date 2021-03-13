package com.zjj.rpc.context;

import com.zjj.common.JRpcURLParamType;
import com.zjj.rpc.Request;
import com.zjj.rpc.Response;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RpcContext {
    private static final ThreadLocal<RpcContext> LOCAL_CONTEXT = ThreadLocal.withInitial(RpcContext::new);

    private RpcContext() {
    }

    private final Map<Object, Object> attributes = new HashMap<>();
    private final Map<String, String> attachments = new HashMap<>();
    private Request request;
    private Response response;
    private String requestIdFromClient;

    public static RpcContext getRpcContext() {
        return LOCAL_CONTEXT.get();
    }

    public static RpcContext init(Request request) {
        RpcContext rpcContext = new RpcContext();
        if (request != null) {
            rpcContext.setRequest(request);
            rpcContext.setRequestIdFromClient(request.getAttachment(JRpcURLParamType.REQUEST_ID_FROM_CLIENT.getName()));
        }
        LOCAL_CONTEXT.set(rpcContext);
        return rpcContext;
    }

    public static RpcContext init() {
        RpcContext rpcContext = new RpcContext();
        LOCAL_CONTEXT.set(rpcContext);
        return rpcContext;
    }

    public static void destroy() {
        LOCAL_CONTEXT.remove();
    }


    public String getRequestId() {
        if (requestIdFromClient != null) {
            return requestIdFromClient;
        }
        if (request != null) {
            return String.valueOf(request.getRequestId());
        }
        return null;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public Map<String, String> getAttachments() {
        return Collections.unmodifiableMap(attachments);
    }

    public void setAttachment(String key, String value) {
        attachments.put(key, value);
    }

    public String getAttachment(String key) {
        return attachments.get(key);
    }

    public String removeAttachment(String key) {
        return attachments.remove(key);
    }

    public String getRequestIdFromClient() {
        return requestIdFromClient;
    }

    public void setRequestIdFromClient(String requestIdFromClient) {
        this.requestIdFromClient = requestIdFromClient;
    }

    public Map<Object, Object> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    public void setAttributes(Object key, Object value) {
        attributes.put(key, value);
    }

    public Object getAttributes(Object key) {
        return attributes.get(key);
    }

    public Object removeAttributes(Object key) {
        return attributes.remove(key);
    }
}
