package com.zjj.jrpc.registry;

import com.zjj.jrpc.common.JRpcURL;

public interface RegistryFactory {
    Registry getRegistry(JRpcURL url);
}
