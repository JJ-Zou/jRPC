package com.zjj.registry.zookeeper;

import com.zjj.common.JRpcURL;
import com.zjj.registry.NotifyListener;
import com.zjj.registry.support.FailbackRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.KeeperException.NodeExistsException;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ZookeeperRegistry extends FailbackRegistry {

    private final Set<String> persistentExistNodePath = ConcurrentHashMap.newKeySet();
    private final Set<JRpcURL> availableServices = ConcurrentHashMap.newKeySet();


    private final CuratorFramework client;

    public ZookeeperRegistry(JRpcURL url, CuratorFramework client) {
        super(url);
        this.client = client;
    }

    @Override
    protected void doRegister(JRpcURL url) {
        removeNode(url, ZkNodeType.AVAILABLE_SERVICE);
        createNode(url, ZkNodeType.UNAVAILABLE_SERVICE);
    }

    @Override
    protected void doUnregister(JRpcURL url) {
        removeNode(url, ZkNodeType.AVAILABLE_SERVICE);
        removeNode(url, ZkNodeType.UNAVAILABLE_SERVICE);
    }

    @Override
    protected void doSubscribe(JRpcURL url, NotifyListener listener) {

    }

    @Override
    protected void doUnsubscribe(JRpcURL url, NotifyListener listener) {

    }

    @Override
    protected void doAvailable(JRpcURL url) {
        if (url == null) {
            for (JRpcURL jRpcURL : getRegisteredServices()) {
                removeNode(jRpcURL, ZkNodeType.UNAVAILABLE_SERVICE);
                createNode(jRpcURL, ZkNodeType.AVAILABLE_SERVICE);
                availableServices.add(jRpcURL);
            }
        } else {
            removeNode(url, ZkNodeType.UNAVAILABLE_SERVICE);
            createNode(url, ZkNodeType.AVAILABLE_SERVICE);
            availableServices.add(url);
        }
    }

    @Override
    protected void doUnavailable(JRpcURL url) {
        if (url == null) {
            for (JRpcURL jRpcURL : getRegisteredServices()) {
                removeNode(jRpcURL, ZkNodeType.AVAILABLE_SERVICE);
                createNode(jRpcURL, ZkNodeType.UNAVAILABLE_SERVICE);
                availableServices.remove(jRpcURL);
            }
        } else {
            removeNode(url, ZkNodeType.AVAILABLE_SERVICE);
            createNode(url, ZkNodeType.UNAVAILABLE_SERVICE);
            availableServices.remove(url);
        }
    }

    @Override
    protected List<JRpcURL> doDiscover(JRpcURL url) {
        return null;
    }

    private void createNode(JRpcURL url, ZkNodeType nodeType) {
        String path = ZkUtils.toNodeTypePath(url, nodeType);
        create(path, true);
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
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private void createPersistent(String path) {
        try {
            client.create().forPath(path);
        } catch (NodeExistsException e) {
            log.warn("ZNode {} already exist.", path, e);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
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
            throw new IllegalStateException(e.getMessage(), e);
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
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private void deletePath(String path) {
        try {
            client.delete().deletingChildrenIfNeeded().forPath(path);
        } catch (NoNodeException e) {

        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
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
            throw new IllegalStateException(e.getMessage(), e);
        }
        return null;
    }

    private List<String> getChildren(String path) {
        try {
            return client.getChildren().forPath(path);
        } catch (NoNodeException e) {
            return null;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
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
}
