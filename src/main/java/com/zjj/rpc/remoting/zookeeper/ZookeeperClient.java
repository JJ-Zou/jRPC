package com.zjj.rpc.remoting.zookeeper;

import com.zjj.rpc.common.JRpcURL;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

import static com.zjj.rpc.remoting.zookeeper.EventType.*;

@Slf4j
public class ZookeeperClient {
    protected int DEFAULT_CONNECTION_TIMEOUT_MS = 5 * 1000;
    protected int DEFAULT_SESSION_TIMEOUT_MS = 60 * 1000;

    private static final String ZK_SESSION_EXPIRE_KEY = "zk.session.expire";
    private static final String TIMEOUT_KEY = "timeout";


    static final Charset CHARSET = Charset.forName("UTF-8");

    private final CuratorFramework client;

    private final JRpcURL url;

    private final Set<StateListener> stateListeners = new CopyOnWriteArraySet<>();

    private final Map<String, TreeCache> treeCacheMap = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, ConcurrentMap<ChildListener, CuratorWatcherImpl>> childListeners = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, ConcurrentMap<DataListener, CuratorWatcherImpl>> listeners = new ConcurrentHashMap<>();

    private volatile boolean closed = false;

    private final Set<String> persistentExistNodePath = ConcurrentHashMap.newKeySet();

    public ZookeeperClient(JRpcURL url) {
        try {
            this.url = url;
            int timeout = url.getParameter(TIMEOUT_KEY, DEFAULT_CONNECTION_TIMEOUT_MS);
            int sessionExpireMs = url.getParameter(ZK_SESSION_EXPIRE_KEY, DEFAULT_SESSION_TIMEOUT_MS);
            CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
                    .connectString(url.getBackupAddress())
                    .retryPolicy(new RetryNTimes(1, 1000))
                    .connectionTimeoutMs(timeout)
                    .sessionTimeoutMs(sessionExpireMs);
            client = builder.build();
            client.getConnectionStateListenable().addListener(new CuratorConnectionStateListener(url));
            client.start();
            boolean connected = client.blockUntilConnected(timeout, TimeUnit.MILLISECONDS);
            if (!connected) {
                throw new IllegalStateException("zookeeper not connected.");
            }
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }


    public JRpcURL getUrl() {
        return url;
    }


    public void delete(String path) {
        persistentExistNodePath.remove(path);
        deletePath(path);
    }


    public void create(String path, boolean ephemeral) {
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


    public void create(String path, String content, boolean ephemeral) {
        if (checkExists(path)) {
            delete(path);
        }
        int i = path.lastIndexOf('/');
        if (i > 0) {
            create(path.substring(0, i), false);
        }
        if (ephemeral) {
            createEphemeral(path, content);
        } else {
            createPersistent(path, content);
        }
    }


    public String getContent(String path) {
        if (!checkExists(path)) {
            return null;
        }
        return doGetContent(path);
    }


    public void addStateListener(StateListener listener) {
        stateListeners.add(listener);
    }


    public void removeStateListener(StateListener listener) {
        stateListeners.remove(listener);
    }

    public Set<StateListener> getSessionListeners() {
        return stateListeners;
    }


    public List<String> addChildrenListener(String path, ChildListener listener) {
        CuratorWatcherImpl targetChildListener = childListeners
                .computeIfAbsent(path, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(listener, k -> createTargetChildListener(path, k));
        return addTargetChildListener(path, targetChildListener);
    }


    public void removeChildrenListener(String path, ChildListener listener) {
        ConcurrentMap<ChildListener, CuratorWatcherImpl> targetChildListeners = childListeners.get(path);
        if (targetChildListeners != null) {
            CuratorWatcherImpl removed = targetChildListeners.remove(listener);
            if (removed != null) {
                removeTargetChildListener(path, removed);
            }
        }
    }


    public void addDataListener(String path, DataListener listener) {
        this.addDataListener(path, listener, null);
    }


    public void addDataListener(String path, DataListener listener, Executor executor) {
        CuratorWatcherImpl targetDataListener = listeners
                .computeIfAbsent(path, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(listener, k -> createTargetDataListener(path, k));
        addTargetDataListener(path, targetDataListener, executor);
    }


    public void removeDataListener(String path, DataListener listener) {
        ConcurrentMap<DataListener, CuratorWatcherImpl> targetDataListeners = listeners.get(path);
        if (targetDataListeners != null) {
            CuratorWatcherImpl removed = targetDataListeners.remove(listener);
            if (removed != null) {
                removeTargetDataListener(path, removed);
            }
        }
    }

    protected void stateChanged(int state) {
        for (StateListener stateListener : getSessionListeners()) {
            stateListener.statedChanged(state);
        }
    }


    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        try {
            doClose();
        } catch (Throwable t) {
            log.warn("{}", t.getMessage(), t);
        }
    }


    public boolean isConnected() {
        return client.getZookeeperClient().isConnected();
    }


    public void createPersistent(String path) {
        try {
            client.create().forPath(path);
        } catch (NodeExistsException e) {
            log.warn("ZNode {} already exist.", path, e);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }


    protected void createPersistent(String path, String data) {
        byte[] dataBytes = data.getBytes(CHARSET);
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


    public void createEphemeral(String path) {
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


    protected void createEphemeral(String path, String data) {
        byte[] dataBytes = data.getBytes(CHARSET);
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


    protected void deletePath(String path) {
        try {
            client.delete().deletingChildrenIfNeeded().forPath(path);
        } catch (NoNodeException e) {

        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }


    public List<String> getChildren(String path) {
        try {
            return client.getChildren().forPath(path);
        } catch (NoNodeException e) {
            return null;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }


    public boolean checkExists(String path) {
        try {
            if (client.checkExists().forPath(path) != null) {
                return true;
            }
        } catch (Exception e) {

        }
        return false;
    }


    public String doGetContent(String path) {
        try {
            byte[] dataBytes = client.getData().forPath(path);
            if (dataBytes == null || dataBytes.length == 0) {
                return null;
            }
            return new String(dataBytes, CHARSET);
        } catch (NoNodeException e) {

        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        return null;
    }


    public CuratorWatcherImpl createTargetChildListener(String path, ChildListener listener) {
        return new CuratorWatcherImpl(client, listener, path);
    }


    public List<String> addTargetChildListener(String path, CuratorWatcherImpl listener) {
        try {
            return client.getChildren().usingWatcher(listener).forPath(path);
        } catch (NoNodeException e) {
            return null;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }


    public void removeTargetChildListener(String path, CuratorWatcherImpl listener) {
        listener.unwatch();
    }


    protected CuratorWatcherImpl createTargetDataListener(String path, DataListener listener) {
        return new CuratorWatcherImpl(client, listener);
    }


    protected void addTargetDataListener(String path, CuratorWatcherImpl listener) {
        this.addTargetDataListener(path, listener, null);
    }


    protected void addTargetDataListener(String path, CuratorWatcherImpl treeCacheListener, Executor executor) {
        try {
            TreeCache treeCache = TreeCache.newBuilder(client, path).setCacheData(false).build();
            treeCacheMap.putIfAbsent(path, treeCache);
            if (executor == null) {
                treeCache.getListenable().addListener(treeCacheListener);
            } else {
                treeCache.getListenable().addListener(treeCacheListener, executor);
            }
            treeCache.start();
        } catch (Exception e) {
            throw new IllegalStateException("Add treeCache listener for path: " + path, e);
        }
    }


    protected void removeTargetDataListener(String path, CuratorWatcherImpl treeCacheListener) {
        TreeCache treeCache = treeCacheMap.get(path);
        if (treeCache != null) {
            treeCache.getListenable().removeListener(treeCacheListener);
        }
        treeCacheListener.dataListener = null;
    }


    public void doClose() {
        client.close();
    }

    static class CuratorWatcherImpl implements CuratorWatcher, TreeCacheListener {

        private CuratorFramework client;
        private volatile ChildListener childListener;
        private volatile DataListener dataListener;
        private String path;

        public CuratorWatcherImpl(CuratorFramework client, ChildListener childListener, String path) {
            this.client = client;
            this.childListener = childListener;
            this.path = path;
        }

        public CuratorWatcherImpl(CuratorFramework client, DataListener dataListener) {
            this.client = client;
            this.dataListener = dataListener;
        }

        public CuratorWatcherImpl() {
        }

        public void unwatch() {
            this.childListener = null;
        }


        public void process(WatchedEvent watchedEvent) throws Exception {
            if (watchedEvent.getType() == Watcher.Event.EventType.None) {
                return;
            }
            if (childListener != null) {
                childListener.childChanged(path, client.getChildren().usingWatcher(this).forPath(path));
            }
        }


        public void childEvent(CuratorFramework curatorFramework, TreeCacheEvent treeCacheEvent) throws Exception {
            if (dataListener == null) {
                return;
            }
            log.debug("listen the zookeeper changed. The changed data: {}.", treeCacheEvent.getData());
            TreeCacheEvent.Type type = treeCacheEvent.getType();
            EventType eventType;
            String path = null;
            String content = null;
            switch (type) {
                case NODE_ADDED:
                    eventType = NodeCreated;
                    path = treeCacheEvent.getData().getPath();
                    content = treeCacheEvent.getData().getData() == null ? "" : new String(treeCacheEvent.getData().getData(), CHARSET);
                    break;
                case NODE_UPDATED:
                    eventType = NodeDataChanged;
                    path = treeCacheEvent.getData().getPath();
                    content = treeCacheEvent.getData().getData() == null ? "" : new String(treeCacheEvent.getData().getData(), CHARSET);
                    break;
                case NODE_REMOVED:
                    eventType = NodeDeleted;
                    path = treeCacheEvent.getData().getPath();
                    break;
                case INITIALIZED:
                    eventType = INITIALIZED;
                    break;
                case CONNECTION_LOST:
                    eventType = CONNECTION_LOST;
                    break;
                case CONNECTION_RECONNECTED:
                    eventType = CONNECTION_RECONNECTED;
                    break;
                case CONNECTION_SUSPENDED:
                    eventType = CONNECTION_SUSPENDED;
                    break;
                default:
                    throw new IllegalStateException("Unknown treeCacheEvent.");
            }
            dataListener.dataChanged(path, content, eventType);
        }
    }

    private class CuratorConnectionStateListener implements ConnectionStateListener {
        private final long UNKNOWN_SESSION_ID = -1L;

        private long lastSessionId;
        private JRpcURL url;

        public CuratorConnectionStateListener(JRpcURL url) {
            this.url = url;
        }


        public void stateChanged(CuratorFramework curatorFramework, ConnectionState connectionState) {
            int timeout = url.getParameter(TIMEOUT_KEY, DEFAULT_CONNECTION_TIMEOUT_MS);
            int sessionExpireMs = url.getParameter(ZK_SESSION_EXPIRE_KEY, DEFAULT_SESSION_TIMEOUT_MS);
            long sessionId = UNKNOWN_SESSION_ID;
            try {
                sessionId = curatorFramework.getZookeeperClient().getZooKeeper().getSessionId();
            } catch (Exception e) {
                log.warn("Curator client state changed, but failed to get the related zk session instance.");
            }
            switch (connectionState) {
                case LOST:
                    log.warn("Curator zookeeper session {} expired.", Long.toHexString(lastSessionId));
                    ZookeeperClient.this.stateChanged(StateListener.SESSION_LOST);
                    break;
                case SUSPENDED:
                    log.warn("Curator zookeeper connection of session {} timed out. connection timeout value is {}, session expire timeout value is {}.",
                            Long.toHexString(sessionId), timeout, sessionExpireMs);
                    ZookeeperClient.this.stateChanged(StateListener.SUSPENDED);
                    break;
                case CONNECTED:
                    lastSessionId = sessionId;
                    log.debug("Curator zookeeper client instance initiated successfully, session id is {}.", Long.toHexString(sessionId));
                    ZookeeperClient.this.stateChanged(StateListener.CONNECTED);
                    break;
                case RECONNECTED:
                    if (lastSessionId == sessionId && sessionId != UNKNOWN_SESSION_ID) {
                        log.warn("Curator zookeeper connection recovered from connection lose, reuse the old session {}.", Long.toHexString(sessionId));
                        ZookeeperClient.this.stateChanged(StateListener.RECONNECTED);
                    } else {
                        log.warn("New session created after old session lost, old session {}, new session {}.", Long.toHexString(lastSessionId), Long.toHexString(sessionId));
                        lastSessionId = sessionId;
                        ZookeeperClient.this.stateChanged(StateListener.NEW_SESSION_CREATED);
                    }
                    break;
                case READ_ONLY:
                    log.debug("connectionState read only.");
                    break;
                default:
                    throw new IllegalStateException("Unknown connectionState.");
            }
        }
    }
}
