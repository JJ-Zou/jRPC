package com.zjj.remoting.zookeeper;

import com.zjj.common.URL;
import com.zjj.registry.zookeeper.ZookeeperClient;
import com.zjj.remoting.support.AbstractZookeeperTransporter;

public class CuratorZookeeperTransporter extends AbstractZookeeperTransporter {
    @Override
    protected ZookeeperClient createZookeeperClient(URL url) {
        return new CuratorZookeeperClient(url);
    }
}
