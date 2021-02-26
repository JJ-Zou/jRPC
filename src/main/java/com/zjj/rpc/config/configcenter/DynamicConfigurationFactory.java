package com.zjj.rpc.config.configcenter;

import com.zjj.rpc.common.JRpcURL;
import com.zjj.rpc.config.annotation.SPI;

@SPI("zookeeper")
public interface DynamicConfigurationFactory {
    DynamicConfiguration getDynamicConfiguration(JRpcURL url);
}
