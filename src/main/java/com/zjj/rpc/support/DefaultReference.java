package com.zjj.rpc.support;

import com.zjj.common.JRpcURL;
import com.zjj.common.JRpcURLParamType;
import com.zjj.extension.ExtensionLoader;
import com.zjj.rpc.Request;
import com.zjj.rpc.Response;
import com.zjj.transport.Client;
import com.zjj.transport.EndpointFactory;

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
        request.setAttachment(JRpcURLParamType.group.getName(), serviceUrl.getGroup());
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
