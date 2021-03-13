package com.zjj.rpc;


public interface ProviderProtectedStrategy {
    Response call(Request request, Provider<?> provider);
}
