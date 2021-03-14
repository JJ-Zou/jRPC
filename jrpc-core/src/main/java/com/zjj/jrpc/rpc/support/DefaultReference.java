package com.zjj.jrpc.rpc.support;

import com.zjj.jrpc.common.JRpcURL;
import com.zjj.jrpc.common.JRpcURLParamType;
import com.zjj.jrpc.extension.ExtensionLoader;
import com.zjj.jrpc.rpc.Request;
import com.zjj.jrpc.rpc.Response;
import com.zjj.jrpc.transport.Client;
import com.zjj.jrpc.transport.EndpointFactory;

import java.io.IOException;

public class DefaultReference<T> extends AbstractReference<T> {

    private final Client client;
    private final EndpointFactory endpointFactory;

    public DefaultReference(Class<T> clazz, JRpcURL url, JRpcURL serviceUrl) {
        super(clazz, url, serviceUrl);
        this.endpointFactory = ExtensionLoader.getExtensionLoader(EndpointFactory.class).getDefaultExtension();
        this.client = endpointFactory.createClient(url);
    }

    @Override
    protected Response doCall(Request request) {
        request.setAttachment(JRpcURLParamType.GROUP.getName(), serviceUrl.getGroup());
        try {
            return client.request(request);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    protected void decrActiveReferCount(Request request, Response response) {
    }

    @Override
    protected boolean doInit() {
        return client.open();
    }

    @Override
    public boolean isAvailable() {
        return client.isAvailable();
    }

    @Override
    public void destroy() {
        endpointFactory.releaseResource(client, url);
    }
}
