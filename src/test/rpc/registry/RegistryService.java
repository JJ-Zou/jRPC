package com.zjj.rpc.registry;


import com.zjj.rpc.common.URL;

import java.util.List;

public interface RegistryService {
    void register(URL url);

    void unregister(URL url);

    void subscribe(URL url, NotifyListener listener);

    void unsubscribe(URL url, NotifyListener listener);

    List<URL> lookup(URL url);
}
