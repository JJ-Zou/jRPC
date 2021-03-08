package com.zjj.rpc.support;

import com.zjj.rpc.Provider;
import com.zjj.rpc.ProviderProtectedStrategy;
import com.zjj.rpc.Request;
import com.zjj.rpc.Response;

public class NonProtectedStrategy implements ProviderProtectedStrategy {

    @Override
    public Response call(Request request, Provider<?> provider) {
        return provider.call(request);
    }
}
