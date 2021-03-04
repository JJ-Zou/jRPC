package com.zjj.rpc.remoting.zookeeper;

import com.zjj.rpc.common.JRpcURL;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ZookeeperTransporter {
    private final Map<String, ZookeeperClient> zookeeperClientMap = new ConcurrentHashMap<>();

    public ZookeeperClient connect(JRpcURL url) {
        ZookeeperClient zookeeperClient;
        List<String> addressList = Collections.singletonList(url.getAddress());
        if ((zookeeperClient = fetchAndUpdateZookeeperClientCache(addressList)) != null && zookeeperClient.isConnected()) {
            log.debug("find valid zookeeper client from the cache for address: {}.", url);
            return zookeeperClient;
        }
        synchronized (zookeeperClientMap) {
            if ((zookeeperClient = fetchAndUpdateZookeeperClientCache(addressList)) != null && zookeeperClient.isConnected()) {
                log.debug("find valid zookeeper client from the cache for address: {}.", url);
                return zookeeperClient;
            }
            zookeeperClient = new ZookeeperClient(url);
            log.debug("No valid zookeeper client found from cache, therefore create a new client for url: {}.", url);
            writeToClientMap(addressList, zookeeperClient);
        }
        return zookeeperClient;
    }

    ZookeeperClient fetchAndUpdateZookeeperClientCache(List<String> addressList) {
        ZookeeperClient zookeeperClient = null;
        for (String address : addressList) {
            if ((zookeeperClient = zookeeperClientMap.get(address)) != null && zookeeperClient.isConnected()) {
                break;
            }
        }
        if (zookeeperClient != null && zookeeperClient.isConnected()) {
            writeToClientMap(addressList, zookeeperClient);
        }
        return zookeeperClient;
    }

    void writeToClientMap(List<String> addressList, ZookeeperClient zookeeperClient) {
        for (String address : addressList) {
            zookeeperClientMap.put(address, zookeeperClient);
        }
    }

}
