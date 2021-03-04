package com.zjj.rpc.registry.zookeeper;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.zjj.dubbo.common.constants.RegistryConstants.*;
import static com.zjj.dubbo.remoting.StateListener.*;

@Slf4j
public class ZookeeperRegistry extends FailbackRegistry {

    private final String root;
    private final Set<String> anyServices = new ConcurrentHashSet<>();
    private final ConcurrentMap<URL, ConcurrentMap<NotifyListener, ChildListener>> zkListeners = new ConcurrentHashMap<>();
    private final ZookeeperClient zkClient;

    public ZookeeperRegistry(URL url, ZookeeperTransporter zookeeperTransporter) {
        super(url);
        if (url.isAnyHost()) {
            throw new IllegalStateException("registry address == null.");
        }
        String group = url.getParameter(CommonConstants.GROUP_KEY, CommonConstants.DEFAULT_ROOT);
        if (!group.startsWith(CommonConstants.PATH_SEPARATOR)) {
            group = CommonConstants.PATH_SEPARATOR + group;
        }
        this.root = group;
        this.zkClient = zookeeperTransporter.connect(url);
        this.zkClient.addStateListener(state -> {
            switch (state) {
                case SESSION_LOST:
                    log.warn("Url of this instance will be deleted from registry soon. Dubbo client will try to re-register once a new session is created.");
                    break;
                case CONNECTED:
                    log.debug("{} connected", this);
                    break;
                case RECONNECTED:
                    log.warn("Trying to fetch the latest urls, in case there're provider changes during connection loss.\n" +
                            " Since ephemeral ZNode will not get deleted for a connection lose, " +
                            "there's no need to re-register url of this instance.");
                    ZookeeperRegistry.this.fetchLatestAddresses();
                    break;
                case SUSPENDED:
                    log.debug("{} suspended", this);
                    break;
                case NEW_SESSION_CREATED:
                    log.warn("Trying to re-register urls and re-subscribe listeners of this instance to registry...");
                    try {
                        ZookeeperRegistry.this.recover();
                    } catch (Exception e) {
                        log.error("{}", e.getMessage(), e);
                    }
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + state);
            }
        });
    }

    @Override
    public boolean isAvailable() {
        return zkClient.isConnected();
    }

    @Override
    public void destroy() {
        super.destroy();
        try {
            zkClient.close();
        } catch (Exception e) {
            log.warn("Failed to close zookeeper client {}, cause: {}", getUrl(), e.getMessage(), e);
        }
    }

    @Override
    public void doRegister(URL url) {
        try {
            zkClient.create(toUrlPath(url), url.getParameter(DYNAMIC_KEY, true));
        } catch (Exception e) {
            log.warn("Failed to register {} to zookeeper {}, cause: {}", url, getUrl(), e.getMessage(), e);
        }
    }

    @Override
    public void doUnregister(URL url) {
        try {
            zkClient.delete(toUrlPath(url));
        } catch (Exception e) {
            log.warn("Failed to unregister {} to zookeeper {}, cause: {}", url, getUrl(), e.getMessage(), e);
        }
    }

    @Override
    public void doSubscribe(URL url, NotifyListener listener) {
        try {
            if (CommonConstants.ANY_VALUE.equals(url.getServiceInterface())) {

            } else {
                List<URL> urls = new ArrayList<>();
                for (String path : toCategoriesPath(url)) {
                    ConcurrentMap<NotifyListener, ChildListener> listeners = zkListeners.computeIfAbsent(url, m -> new ConcurrentHashMap<>());
//todo
                    zkClient.create(path, false);
                    List<String> children = zkClient.addChildrenListener(path, null);

                }
                notify(url, listener, urls);
            }
        } catch (Throwable e) {
            throw new RpcException("Failed to subscribe " + url + " to zookeeper " + getUrl() + ", cause: " + e.getMessage(), e);
        }
    }

    @Override
    public void doUnsubscribe(URL url, NotifyListener listener) {

    }

    private String toCategoryPath(URL url) {
        return toServicePath(url) + CommonConstants.PATH_SEPARATOR + url.getParameter(CATEGORY_KEY, DEFAULT_CATEGORY);
    }

    private String[] toCategoriesPath(URL url) {
        String[] categories;
        if (CommonConstants.ANY_VALUE.equals(url.getParameter(CATEGORY_KEY))) {
            categories = new String[]{PROVIDERS_CATEGORY, CONSUMERS_CATEGORY, ROUTERS_CATEGORY, CONFIGURATORS_CATEGORY};
        } else {
            categories = url.getParameter(CATEGORY_KEY, new String[]{DEFAULT_CATEGORY});
        }
        String[] paths = new String[categories.length];
        for (int i = 0; i < categories.length; i++) {
            paths[i] = toServicePath(url) + CommonConstants.PATH_SEPARATOR + categories[i];
        }
        return paths;
    }


    private String toUrlPath(URL url) {
        return toServicePath(url) + CommonConstants.PATH_SEPARATOR + url.getParameter(CATEGORY_KEY, DEFAULT_CATEGORY);
    }


    private String toServicePath(URL url) {
        String name = url.getServiceInterface();
        if (CommonConstants.ANY_VALUE.equals(name)) {
            return toRootPath();
        }
        return toRootDir() + URL.encode(name);
    }

    private String toRootDir() {
        if (root.equals(CommonConstants.PATH_SEPARATOR)) {
            return root;
        }
        return root + CommonConstants.PATH_SEPARATOR;
    }

    private String toRootPath() {
        return root;
    }

    private void fetchLatestAddresses() {
        HashMap<URL, Set<NotifyListener>> recoverSubscribed = new HashMap<>(getSubscribed());
        if (!recoverSubscribed.isEmpty()) {
            log.debug("Fetching the latest urls of {}", recoverSubscribed.keySet());
            for (Map.Entry<URL, Set<NotifyListener>> entry : recoverSubscribed.entrySet()) {
                URL url = entry.getKey();
                for (NotifyListener listener : entry.getValue()) {
                    addFailedSubscribed(url, listener);
                }
            }
        }
    }
}
