package com.zjj.rpc.config.configcenter;

import com.zjj.rpc.common.JRpcURL;

public interface DynamicConfigurationFactory {
    DynamicConfiguration getDynamicConfiguration(JRpcURL url);
}
