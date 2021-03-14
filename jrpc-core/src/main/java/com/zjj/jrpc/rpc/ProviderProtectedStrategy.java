package com.zjj.jrpc.rpc;


public interface ProviderProtectedStrategy {
    Response call(Request request, Provider<?> provider);
}
