package com.zjj.jrpc.rpc.support;

import com.zjj.jrpc.rpc.Provider;
import com.zjj.jrpc.rpc.ProviderProtectedStrategy;
import com.zjj.jrpc.rpc.Request;
import com.zjj.jrpc.rpc.Response;

public class NonProtectedStrategy implements ProviderProtectedStrategy {

    @Override
    public Response call(Request request, Provider<?> provider) {
        return provider.call(request);
    }
}
