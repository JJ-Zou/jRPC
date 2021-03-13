package com.zjj.registry.zookeeper;

import com.zjj.common.JRpcURL;
import com.zjj.exception.JRpcErrorMessage;
import com.zjj.exception.JRpcFrameworkException;
import com.zjj.registry.ServiceListener;
import com.zjj.registry.support.ServiceFailbackRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.KeeperException.NodeExistsException;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class ZookeeperRegistry extends ServiceFailbackRegistry implements Closeable {

    private final Set<String> persistentExistNodePath = ConcurrentHashMap.newKeySet();
    private final Set<JRpcURL> availableServices = ConcurrentHashMap.newKeySet();
    private final ConcurrentMap<JRpcURL, Map<ServiceListener, TreeCacheListener>> serviceListeners = new ConcurrentHashMap<>();
    private final CuratorFramework client;
    private final TreeCache treeCache;

    public ZookeeperRegistry(JRpcURL url, CuratorFramework client) {
        super(url);
        this.client = client;
        client.getConnectionStateListenable().addListener((c, state) -> {
            log.info("CuratorFramework {} change the state {}", client.getClass().getSimpleName(), state);
            switch (state) {
                case LOST:
                    // todo: session 失效重新写入节点
                    break;
                case CONNECTED:
                case READ_ONLY:
                case SUSPENDED:
                case RECONNECTED:
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + state);
            }
        });
        this.treeCache = TreeCache.newBuilder(client, ZkUtils.toNodeTypePath(url, ZkNodeType.AVAILABLE_SERVICE)).build();
        try {
            this.treeCache.start();
        } catch (Exception e) {
            log.warn("TreeCache start err.", e);
        }
    }

    @Override
    protected void doRegister(JRpcURL url) {
        removeNode(url, ZkNodeType.UNAVAILABLE_SERVICE);
        removeNode(url, ZkNodeType.AVAILABLE_SERVICE);
        createNode(url, ZkNodeType.UNAVAILABLE_SERVICE);
    }

    @Override
    protected void doUnregister(JRpcURL url) {
        removeNode(url, ZkNodeType.AVAILABLE_SERVICE);
        removeNode(url, ZkNodeType.UNAVAILABLE_SERVICE);
    }

    @Override
    protected void subscribeService(JRpcURL url, ServiceListener serviceListener) {
        Map<ServiceListener, TreeCacheListener> treeCacheListeners = serviceListeners.computeIfAbsent(url, map -> new ConcurrentHashMap<>());
        TreeCacheListener treeCacheListener = treeCacheListeners
                .computeIfAbsent(serviceListener, tl -> new TreeCachedListerImpl(this, serviceListener, url));
        removeNode(url, ZkNodeType.CLIENT);
        createNode(url, ZkNodeType.CLIENT);
        treeCache.getListenable().addListener(treeCacheListener);
        log.info("[ZookeeperRegistry] subscribe service: path = {}, info= {}", ZkUtils.toNodePath(url, ZkNodeType.AVAILABLE_SERVICE), url.toFullString());
    }

    @Override
    protected void unsubscribeService(JRpcURL url, ServiceListener serviceListener) {
        Map<ServiceListener, TreeCacheListener> listenerMap = serviceListeners.get(url);
        if (listenerMap == null) {
            return;
        }
        TreeCacheListener treeCacheListener = listenerMap.remove(serviceListener);
        if (treeCacheListener != null) {
            treeCache.getListenable().removeListener(treeCacheListener);
        }
        log.info("[ZookeeperRegistry] unsubscribe service: path = {}.", url);
    }

    @Override
    protected void doAvailable(JRpcURL url) {
        if (url == null) {
            for (JRpcURL jRpcURL : getRegisteredServices()) {
                removeNode(jRpcURL, ZkNodeType.AVAILABLE_SERVICE);
                removeNode(jRpcURL, ZkNodeType.UNAVAILABLE_SERVICE);
                createNode(jRpcURL, ZkNodeType.AVAILABLE_SERVICE);
                availableServices.add(jRpcURL);
            }
        } else {
            removeNode(url, ZkNodeType.AVAILABLE_SERVICE);
            removeNode(url, ZkNodeType.UNAVAILABLE_SERVICE);
            createNode(url, ZkNodeType.AVAILABLE_SERVICE);
            availableServices.add(url);
        }
    }

    @Override
    protected void doUnavailable(JRpcURL url) {
        if (url == null) {
            for (JRpcURL jRpcURL : getRegisteredServices()) {
                removeNode(jRpcURL, ZkNodeType.UNAVAILABLE_SERVICE);
                removeNode(jRpcURL, ZkNodeType.AVAILABLE_SERVICE);
                createNode(jRpcURL, ZkNodeType.UNAVAILABLE_SERVICE);
                availableServices.remove(jRpcURL);
            }
        } else {
            removeNode(url, ZkNodeType.UNAVAILABLE_SERVICE);
            removeNode(url, ZkNodeType.AVAILABLE_SERVICE);
            createNode(url, ZkNodeType.UNAVAILABLE_SERVICE);
            availableServices.remove(url);
        }
    }


    @Override
    protected List<JRpcURL> discoverService(JRpcURL url) {
        String parentPath = ZkUtils.toNodeTypePath(url, ZkNodeType.AVAILABLE_SERVICE);
        if (!checkExists(parentPath)) {
            return Collections.emptyList();
        }
        return nodeTChildrenUrls(url, parentPath);
    }

    private void createNode(JRpcURL url, ZkNodeType nodeType) {
        String nodeTypePath = ZkUtils.toNodeTypePath(url, nodeType);
        if (checkExists(nodeTypePath)) {
            create(nodeTypePath, false);
        }
        String path = ZkUtils.toNodePath(url, nodeType);
        create(path, url.toFullString(), true);
    }

    private void removeNode(JRpcURL url, ZkNodeType nodeType) {
        String path = ZkUtils.toNodeTypePath(url, nodeType);
        if (checkExists(path)) {
            deletePath(path);
        }
    }

    private void create(String path, boolean ephemeral) {
        if (!ephemeral) {
            if (persistentExistNodePath.contains(path)) {
                return;
            }
            if (checkExists(path)) {
                persistentExistNodePath.add(path);
                return;
            }
        }
        int i = path.lastIndexOf('/');
        if (i > 0) {
            create(path.substring(0, i), false);
        }
        if (ephemeral) {
            createEphemeral(path);
        } else {
            createPersistent(path);
            persistentExistNodePath.add(path);
        }
    }

    private void createEphemeral(String path) {
        try {
            client.create().withMode(CreateMode.EPHEMERAL).forPath(path);
        } catch (NodeExistsException e) {
            log.warn("ZNode {} already exist, since we will only try to recreate a node on a session expiration," +
                    " this duplication might be caused by a delete delay from the zk server," +
                    " which means the old expired session may still holds this ZNode and the server just hasn't got time to do the deletion." +
                    " In this case, we can just try to delete and create again.", path, e);
            deletePath(path);
            createEphemeral(path);
        } catch (Exception e) {
            throw new JRpcFrameworkException(e.getMessage(), e, JRpcErrorMessage.FRAMEWORK_REGISTER_ERROR);
        }
    }

    private void createPersistent(String path) {
        try {
            client.create().forPath(path);
        } catch (NodeExistsException e) {
            log.warn("ZNode {} already exist.", path, e);
        } catch (Exception e) {
            throw new JRpcFrameworkException(e.getMessage(), e, JRpcErrorMessage.FRAMEWORK_REGISTER_ERROR);
        }
    }

    private void create(String path, String data, boolean ephemeral) {
        if (!ephemeral) {
            if (persistentExistNodePath.contains(path)) {
                return;
            }
            if (checkExists(path)) {
                persistentExistNodePath.add(path);
                return;
            }
        }
        int i = path.lastIndexOf('/');
        if (i > 0) {
            create(path.substring(0, i), false);
        }
        if (ephemeral) {
            createEphemeral(path, data);
        } else {
            createPersistent(path, data);
            persistentExistNodePath.add(path);
        }
    }

    private void createEphemeral(String path, String data) {
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
        try {
            client.create().withMode(CreateMode.EPHEMERAL).forPath(path, dataBytes);
        } catch (NodeExistsException e) {
            log.warn("ZNode {} already exist, since we will only try to recreate a node on a session expiration," +
                    " this duplication might be caused by a delete delay from the zk server," +
                    " which means the old expired session may still holds this ZNode and the server just hasn't got time to do the deletion." +
                    " In this case, we can just try to delete and create again.", path, e);
            deletePath(path);
            createEphemeral(path, data);
        } catch (Exception e) {
            throw new JRpcFrameworkException(e.getMessage(), e, JRpcErrorMessage.FRAMEWORK_REGISTER_ERROR);
        }
    }

    private void createPersistent(String path, String data) {
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
        try {
            client.create().forPath(path, dataBytes);
        } catch (NodeExistsException e) {
            try {
                client.setData().forPath(path, dataBytes);
            } catch (Exception exception) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        } catch (Exception e) {
            throw new JRpcFrameworkException(e.getMessage(), e, JRpcErrorMessage.FRAMEWORK_REGISTER_ERROR);
        }
    }

    private void deletePath(String path) {
        try {
            client.delete().deletingChildrenIfNeeded().forPath(path);
        } catch (NoNodeException e) {

        } catch (Exception e) {
            throw new JRpcFrameworkException(e.getMessage(), e, JRpcErrorMessage.FRAMEWORK_REGISTER_ERROR);
        }
    }

    private String getContent(String path) {
        if (!checkExists(path)) {
            return null;
        }
        return doGetContent(path);
    }

    private String doGetContent(String path) {
        try {
            byte[] dataBytes = client.getData().forPath(path);
            if (dataBytes == null || dataBytes.length == 0) {
                return null;
            }
            return new String(dataBytes, StandardCharsets.UTF_8);
        } catch (NoNodeException e) {

        } catch (Exception e) {
            throw new JRpcFrameworkException(e.getMessage(), e, JRpcErrorMessage.FRAMEWORK_REGISTER_ERROR);
        }
        return null;
    }

    private List<String> getChildren(String path) {
        try {
            return client.getChildren().forPath(path);
        } catch (NoNodeException e) {
            return Collections.emptyList();
        } catch (Exception e) {
            throw new JRpcFrameworkException(e.getMessage(), e, JRpcErrorMessage.FRAMEWORK_REGISTER_ERROR);
        }
    }

    private boolean checkExists(String path) {
        try {
            if (client.checkExists().forPath(path) != null) {
                return true;
            }
        } catch (Exception e) {

        }
        return false;
    }

    private List<JRpcURL> nodeTChildrenUrls(JRpcURL url, String parentPath) {
        List<JRpcURL> ret = new ArrayList<>();
        // 服务节点路径为 host:port
        for (String node : getChildren(parentPath)) {
            String childPath = parentPath + "/" + node;
            String data = getContent(childPath);
            JRpcURL newUrl = null;
            try {
                newUrl = JRpcURL.valueOf(data);
            } catch (Exception e) {
                log.warn("node content {} with path {} parse fail, maybe it's not a service node.", data, childPath);
            }
            if (newUrl == null) {
                String host;
                int port = 14130;
                if (node.contains(":")) {
                    String[] split = node.split(":");
                    host = split[0];
                    port = Integer.parseInt(split[1]);
                } else {
                    host = node;
                }
                // 引用对象，必须深拷贝才能不改变原对象
                newUrl = url.deepCloneWithAddress(host, port);
            }
            ret.add(newUrl);
        }
        return ret;
    }

    @Override
    public void close() throws IOException {
        client.close();
    }

    static class TreeCachedListerImpl implements TreeCacheListener {
        private final ZookeeperRegistry registry;
        private final ServiceListener serviceListener;
        private final JRpcURL url;

        public TreeCachedListerImpl(ZookeeperRegistry registry, ServiceListener serviceListener, JRpcURL url) {
            this.registry = registry;
            this.serviceListener = serviceListener;
            this.url = url;
        }

        @Override
        public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
            ChildData childData = event.getData();
            String path;
            String data;
            log.info("[ZookeeperRegistry] listen event ({}) from path ({})。", event.getType(), childData);
            switch (event.getType()) {
                case NODE_ADDED:
                    path = childData.getPath();
                    data = new String(childData.getData(), StandardCharsets.UTF_8);
                    log.info("NODE_ADDED path [{}] data [{}]。", path, data);
                    serviceListener.notifyService(url, registry.getRegistryUrl(), registry.nodeTChildrenUrls(url, path));
                    break;
                case NODE_UPDATED:
                    path = childData.getPath();
                    data = new String(childData.getData(), StandardCharsets.UTF_8);
                    log.info("NODE_UPDATED path [{}] data [{}]。", path, data);
                    serviceListener.notifyService(url, registry.getRegistryUrl(), registry.nodeTChildrenUrls(url, path));
                    break;
                case NODE_REMOVED:
                    path = childData.getPath();
                    data = new String(childData.getData(), StandardCharsets.UTF_8);
                    log.info("NODE_REMOVED path [{}] data [{}]。", path, data);
                    serviceListener.notifyService(url, registry.getRegistryUrl(), registry.nodeTChildrenUrls(url, path));
                    break;
                case INITIALIZED:
                    break;
                case CONNECTION_LOST:
                    break;
                case CONNECTION_RECONNECTED:
                    break;
                case CONNECTION_SUSPENDED:
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + event);
            }
        }

    }
}
