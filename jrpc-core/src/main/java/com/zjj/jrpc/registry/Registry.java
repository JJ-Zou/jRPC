package com.zjj.jrpc.registry;

import com.zjj.jrpc.common.JRpcURL;

public interface Registry extends RegistryService, DiscoveryService {

    JRpcURL getRegistryUrl();

}
