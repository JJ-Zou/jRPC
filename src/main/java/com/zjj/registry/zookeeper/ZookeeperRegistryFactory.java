package com.zjj.registry.zookeeper;

import com.zjj.common.JRpcURL;
import com.zjj.common.JRpcURLParamType;
import com.zjj.registry.Registry;
import com.zjj.registry.support.AbstractRegistryFactory;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;

import java.util.concurrent.TimeUnit;

public class ZookeeperRegistryFactory extends AbstractRegistryFactory {
    @Override
    protected Registry createRegistry(JRpcURL registryUrl) {
        try {
            int timeout = registryUrl.getParameter(JRpcURLParamType.connectTimeout.getName(), JRpcURLParamType.connectTimeout.getIntValue());
            int sessionTimeout = registryUrl.getParameter(JRpcURLParamType.registrySessionTimeout.getName(), JRpcURLParamType.registrySessionTimeout.getIntValue());
            int retryTimes = registryUrl.getParameter(JRpcURLParamType.registryRetryTimes.getName(), JRpcURLParamType.registryRetryTimes.getIntValue());
            int sleepMsBetweenRetries = registryUrl.getParameter(JRpcURLParamType.sleepMsBetweenRetries.getName(), JRpcURLParamType.sleepMsBetweenRetries.getIntValue());
            String address = registryUrl.getParameter("address");
            CuratorFramework zkClient = CuratorFrameworkFactory.builder()
                    .connectString(address)
                    .retryPolicy(new RetryNTimes(retryTimes, sleepMsBetweenRetries))
                    .connectionTimeoutMs(timeout)
                    .sessionTimeoutMs(sessionTimeout)
                    .build();
            zkClient.start();
            boolean connected = zkClient.blockUntilConnected(timeout, TimeUnit.MILLISECONDS);
            if (!connected) {
                throw new IllegalStateException("zookeeper not connected");
            }
            return new ZookeeperRegistry(registryUrl, zkClient);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

}
