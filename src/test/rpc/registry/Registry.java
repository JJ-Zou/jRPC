package com.zjj.rpc.registry;


import com.zjj.rpc.common.Node;
import com.zjj.rpc.common.URL;

public interface Registry extends Node, RegistryService {
    default void reExportRegister(URL url) {
        register(url);
    }

    default void reExportUnregister(URL url) {
        unregister(url);
    }
}
