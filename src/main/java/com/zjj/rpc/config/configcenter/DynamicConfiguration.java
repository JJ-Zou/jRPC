package com.zjj.rpc.config.configcenter;

import com.zjj.rpc.common.JRpcURL;
import com.zjj.rpc.common.urils.ReflectUtils;
import com.zjj.rpc.extension.ServiceExtensionLoader;

import java.lang.reflect.Method;

public interface DynamicConfiguration extends AutoCloseable {
    static DynamicConfiguration getDynamicConfiguration(JRpcURL connectionURL) {
        String protocol = connectionURL.getProtocol();
        DynamicConfigurationFactory factory = getDynamicConfigurationFactory(protocol);
        return factory.getDynamicConfiguration(connectionURL);
    }

    static DynamicConfigurationFactory getDynamicConfigurationFactory(String protocol) {
        DynamicConfigurationFactory instance = ServiceExtensionLoader
                .getExtensionLoader(DynamicConfigurationFactory.class)
                .getExtension(protocol);
        if (instance == null) {
            throw new IllegalStateException("Cannot find impl of DynamicConfigurationFactory.");
        }
        return instance;
    }

}
