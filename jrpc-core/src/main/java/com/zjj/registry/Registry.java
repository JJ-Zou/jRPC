package com.zjj.registry;

import com.zjj.common.JRpcURL;

public interface Registry extends RegistryService, DiscoveryService {

    JRpcURL getRegistryUrl();

}
