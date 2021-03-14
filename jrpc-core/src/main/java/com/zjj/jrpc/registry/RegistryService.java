package com.zjj.jrpc.registry;

import com.zjj.jrpc.common.JRpcURL;

import java.util.Collection;

public interface RegistryService {
    void register(JRpcURL url);

    void unregister(JRpcURL url);

    void available(JRpcURL url);

    void unavailable(JRpcURL url);

    Collection<JRpcURL> getRegisteredServices();
}
