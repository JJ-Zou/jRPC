package com.zjj.rpc.config.configcenter.support.zookeeper;

import com.zjj.rpc.common.JRpcURL;
import com.zjj.rpc.config.configcenter.AbstractDynamicConfigurationFactory;
import com.zjj.rpc.config.configcenter.DynamicConfiguration;
import com.zjj.rpc.remoting.zookeeper.ZookeeperTransporter;

public class ZookeeperDynamicConfigurationFactory extends AbstractDynamicConfigurationFactory {

    @Override
    protected DynamicConfiguration createDynamicConfiguration(JRpcURL url) {
        return new ZookeeperDynamicConfiguration(url, new ZookeeperTransporter());
    }
}
