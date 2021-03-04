package com.zjj.registry;

import com.zjj.common.JRpcURL;

public interface RegistryFactory {
    Registry getRegistry(JRpcURL url);
}
