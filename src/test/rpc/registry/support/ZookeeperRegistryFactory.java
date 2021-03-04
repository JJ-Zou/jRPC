package com.zjj.rpc.registry.support;

import com.zjj.dubbo.common.URL;
import com.zjj.dubbo.registry.Registry;
import com.zjj.dubbo.registry.zookeeper.ZookeeperRegistry;
import com.zjj.dubbo.remoting.zookeeper.ZookeeperTransporter;

public class ZookeeperRegistryFactory extends AbstractRegistryFactory {

    private ZookeeperTransporter zookeeperTransporter;

    public void setZookeeperTransporter(ZookeeperTransporter zookeeperTransporter) {
        this.zookeeperTransporter = zookeeperTransporter;
    }

    @Override
    protected Registry createRegistry(URL url) {
        return new ZookeeperRegistry(url, zookeeperTransporter);
    }
}
