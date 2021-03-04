package com.zjj.registry;

import com.zjj.common.JRpcURL;

import java.util.Collection;

public interface RegistryService {
    void register(JRpcURL url);

    void unregister(JRpcURL url);

    void available(JRpcURL url);

    void unavailable(JRpcURL url);

    Collection<JRpcURL> getRegisteredServices();
}
