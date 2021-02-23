package com.zjj.registry.support;

import com.zjj.common.URL;
import com.zjj.registry.Registry;
import com.zjj.remoting.ZookeeperTransporter;
import com.zjj.registry.zookeeper.ZookeeperRegistry;

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
