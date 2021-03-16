package com.zjj.jrpc.registry.zookeeper;

import com.zjj.jrpc.common.JRpcURL;
import com.zjj.jrpc.common.JRpcURLParamType;
import com.zjj.jrpc.exception.JRpcErrorMessage;
import com.zjj.jrpc.exception.JRpcFrameworkException;
import com.zjj.jrpc.registry.Registry;
import com.zjj.jrpc.registry.support.AbstractRegistryFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;

import java.util.concurrent.TimeUnit;

@Slf4j
public class ZookeeperRegistryFactory extends AbstractRegistryFactory {
    @Override
    protected Registry createRegistry(JRpcURL registryUrl) {
        try {
            int timeout = registryUrl.getParameter(JRpcURLParamType.CONNECT_TIMEOUT.getName(), JRpcURLParamType.CONNECT_TIMEOUT.getIntValue());
            int sessionTimeout = registryUrl.getParameter(JRpcURLParamType.REGISTRY_SESSION_TIMEOUT.getName(), JRpcURLParamType.REGISTRY_SESSION_TIMEOUT.getIntValue());
            int retryTimes = registryUrl.getParameter(JRpcURLParamType.REGISTRY_RETRY_TIMES.getName(), JRpcURLParamType.REGISTRY_RETRY_TIMES.getIntValue());
            int sleepMsBetweenRetries = registryUrl.getParameter(JRpcURLParamType.SLEEP_MS_BETWEEN_RETRIES.getName(), JRpcURLParamType.SLEEP_MS_BETWEEN_RETRIES.getIntValue());
            String address = registryUrl.getBindAddress();
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
            throw new JRpcFrameworkException(e.getMessage(), e, JRpcErrorMessage.FRAMEWORK_INIT_ERROR);
        }
    }
}
