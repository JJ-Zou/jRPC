package com.zjj.rpc.config.configcenter.support.zookeeper;

import com.zjj.rpc.common.JRpcURL;
import com.zjj.rpc.config.configcenter.DynamicConfiguration;
import com.zjj.rpc.remoting.zookeeper.ZookeeperClient;
import com.zjj.rpc.remoting.zookeeper.ZookeeperTransporter;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ZookeeperDynamicConfiguration implements DynamicConfiguration {
    private Executor executor;
    private String rootPath;
    private ZookeeperClient zkClient;
    private CountDownLatch initializedLatch;
    private JRpcURL url;

    public ZookeeperDynamicConfiguration(JRpcURL url, ZookeeperTransporter zookeeperTransporter) {
        this.url = url;
        this.initializedLatch = new CountDownLatch(1);
        this.executor = Executors.newSingleThreadExecutor();
        this.zkClient = zookeeperTransporter.connect(url);
    }

    @Override
    public void close() throws Exception {
        zkClient.close();
    }
}
