package com.zjj.registry;

import com.zjj.common.Node;
import com.zjj.common.URL;

public interface Registry extends Node, RegistryService {
    default void reExportRegister(URL url) {
        register(url);
    }

    default void reExportUnregister(URL url) {
        unregister(url);
    }
}
