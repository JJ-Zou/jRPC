package com.zjj.remoting.support;

import com.zjj.common.URL;
import com.zjj.common.constants.CommonConstants;
import com.zjj.common.utils.StringUtils;
import com.zjj.registry.zookeeper.ZookeeperClient;
import com.zjj.remoting.ZookeeperTransporter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.zjj.common.RemotingConstants.BACKUP_KEY;

@Slf4j
public abstract class AbstractZookeeperTransporter implements ZookeeperTransporter {
    private final Map<String, ZookeeperClient> zookeeperClientMap = new ConcurrentHashMap<>();

    @Override
    public ZookeeperClient connect(URL url) {
        ZookeeperClient zookeeperClient;
        List<String> addressList = getURLBackupAddress(url);
        if ((zookeeperClient = fetchAndUpdateZookeeperClientCache(addressList)) != null && zookeeperClient.isConnected()) {
            log.debug("find valid zookeeper client from the cache for address: {}.", url);
            return zookeeperClient;
        }
        synchronized (zookeeperClientMap) {
            if ((zookeeperClient = fetchAndUpdateZookeeperClientCache(addressList)) != null && zookeeperClient.isConnected()) {
                log.debug("find valid zookeeper client from the cache for address: {}.", url);
                return zookeeperClient;
            }
            zookeeperClient = createZookeeperClient(url);
            log.debug("No valid zookeeper client found from cache, therefore create a new client for url: {}.", url);
            writeToClientMap(addressList, zookeeperClient);
        }
        return zookeeperClient;
    }

    protected abstract ZookeeperClient createZookeeperClient(URL url);

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

    List<String> getURLBackupAddress(URL url) {
        List<String> addressList = new ArrayList<>();
        addressList.add(url.getAddress());
        addressList.addAll(url.getParameter(BACKUP_KEY, Collections.EMPTY_LIST));
        String authPrefix = null;
        if (StringUtils.isNotEmpty(url.getUsername())) {
            StringBuffer buf = new StringBuffer();
            buf.append(url.getUsername());
            if (StringUtils.isNotEmpty(url.getPassword())) {
                buf.append(":").append(url.getPassword());
            }
            buf.append("@");
            authPrefix = buf.toString();
        }
        if (StringUtils.isNotEmpty(authPrefix)) {
            List<String> authAddressList = new ArrayList<>(addressList.size());
            for (String address : addressList) {
                authAddressList.add(authPrefix + address);
            }
            return authAddressList;
        }
        return addressList;
    }


    URL toClientURL(URL url) {
        Map<String, String> parameterMap = new HashMap<>();
        if (url.getParameter(CommonConstants.TIMEOUT_KEY) != null) {
            parameterMap.put(CommonConstants.TIMEOUT_KEY, url.getParameter(CommonConstants.TIMEOUT_KEY));
        }
        if (url.getParameter(BACKUP_KEY) != null) {
            parameterMap.put(BACKUP_KEY, url.getParameter(BACKUP_KEY));
        }
        return new URL(url.getProtocol(), url.getUsername(), url.getPassword(), url.getHost(), url.getPort(),
                ZookeeperTransporter.class.getName(), parameterMap);
    }

    // 供测试
    Map<String, ZookeeperClient> getZookeeperClientMap() {
        return zookeeperClientMap;
    }

}
