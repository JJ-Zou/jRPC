package com.zjj.registry;

import com.zjj.common.URL;

public interface RegistryFactory {
    Registry getRegistry(URL url);
}
