package com.zjj.remoting;

import com.zjj.common.URL;
import com.zjj.registry.zookeeper.ZookeeperClient;

public interface ZookeeperTransporter {

    ZookeeperClient connect(URL url);
}
