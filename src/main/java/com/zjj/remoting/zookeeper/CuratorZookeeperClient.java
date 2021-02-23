package com.zjj.remoting.zookeeper;

import com.zjj.common.URL;
import com.zjj.common.constants.CommonConstants;
import com.zjj.remoting.ChildListener;
import com.zjj.remoting.DataListener;
import com.zjj.remoting.EventType;
import com.zjj.remoting.StateListener;
import com.zjj.remoting.support.AbstractZookeeperClient;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static com.zjj.remoting.EventType.*;


@Slf4j
public class CuratorZookeeperClient extends AbstractZookeeperClient<CuratorZookeeperClient.CuratorWatcherImpl, CuratorZookeeperClient.CuratorWatcherImpl> {


    private static final String ZK_SESSION_EXPIRE_KEY = "zk.session.expire";

    static final Charset CHARSET = Charset.forName("UTF-8");

    private final CuratorFramework client;

    private Map<String, TreeCache> treeCacheMap = new ConcurrentHashMap<>();

    public CuratorZookeeperClient(URL url) {
        super(url);
        try {
            int timeout = url.getParameter(CommonConstants.TIMEOUT_KEY, DEFAULT_CONNECTION_TIMEOUT_MS);
            int sessionExpireMs = url.getParameter(ZK_SESSION_EXPIRE_KEY, DEFAULT_SESSION_TIMEOUT_MS);
            CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
                    .connectString(url.getBackupAddress())
                    .retryPolicy(new RetryNTimes(1, 1000))
                    .connectionTimeoutMs(timeout)
                    .sessionTimeoutMs(sessionExpireMs);
            String authority = url.getAuthority();
            if (authority != null && authority.length() > 0) {
                builder = builder.authorization("digest", authority.getBytes());
            }
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


    @Override
    public boolean isConnected() {
        return client.getZookeeperClient().isConnected();
    }

    @Override
    public void createPersistent(String path) {
        try {
            client.create().forPath(path);
        } catch (NodeExistsException e) {
            log.warn("ZNode {} already exist.", path, e);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
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

    @Override
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

    @Override
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

    @Override
    protected void deletePath(String path) {
        try {
            client.delete().deletingChildrenIfNeeded().forPath(path);
        } catch (NoNodeException e) {

        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public List<String> getChildren(String path) {
        try {
            return client.getChildren().forPath(path);
        } catch (NoNodeException e) {
            return null;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public boolean checkExists(String path) {
        try {
            if (client.checkExists().forPath(path) != null) {
                return true;
            }
        } catch (Exception e) {

        }
        return false;
    }

    @Override
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

    @Override
    public CuratorWatcherImpl createTargetChildListener(String path, ChildListener listener) {
        return new CuratorWatcherImpl(client, listener, path);
    }

    @Override
    public List<String> addTargetChildListener(String path, CuratorWatcherImpl listener) {
        try {
            return client.getChildren().usingWatcher(listener).forPath(path);
        } catch (NoNodeException e) {
            return null;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public void removeTargetChildListener(String path, CuratorWatcherImpl listener) {
        listener.unwatch();
    }

    @Override
    protected CuratorWatcherImpl createTargetDataListener(String path, DataListener listener) {
        return new CuratorWatcherImpl(client, listener);
    }

    @Override
    protected void addTargetDataListener(String path, CuratorWatcherImpl listener) {
        this.addTargetDataListener(path, listener, null);
    }

    @Override
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

    @Override
    protected void removeTargetDataListener(String path, CuratorWatcherImpl treeCacheListener) {
        TreeCache treeCache = treeCacheMap.get(path);
        if (treeCache != null) {
            treeCache.getListenable().removeListener(treeCacheListener);
        }
        treeCacheListener.dataListener = null;
    }


    @Override
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

        @Override
        public void process(WatchedEvent watchedEvent) throws Exception {
            if (watchedEvent.getType() == Watcher.Event.EventType.None) {
                return;
            }
            if (childListener != null) {
                childListener.childChanged(path, client.getChildren().usingWatcher(this).forPath(path));
            }
        }

        @Override
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
        private URL url;

        public CuratorConnectionStateListener(URL url) {
            this.url = url;
        }


        @Override
        public void stateChanged(CuratorFramework curatorFramework, ConnectionState connectionState) {
            int timeout = url.getParameter(CommonConstants.TIMEOUT_KEY, DEFAULT_CONNECTION_TIMEOUT_MS);
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
                    CuratorZookeeperClient.this.stateChanged(StateListener.SESSION_LOST);
                    break;
                case SUSPENDED:
                    log.warn("Curator zookeeper connection of session {} timed out. connection timeout value is {}, session expire timeout value is {}.",
                            Long.toHexString(sessionId), timeout, sessionExpireMs);
                    CuratorZookeeperClient.this.stateChanged(StateListener.SUSPENDED);
                    break;
                case CONNECTED:
                    lastSessionId = sessionId;
                    log.debug("Curator zookeeper client instance initiated successfully, session id is {}.", Long.toHexString(sessionId));
                    CuratorZookeeperClient.this.stateChanged(StateListener.CONNECTED);
                    break;
                case RECONNECTED:
                    if (lastSessionId == sessionId && sessionId != UNKNOWN_SESSION_ID) {
                        log.warn("Curator zookeeper connection recovered from connection lose, reuse the old session {}.", Long.toHexString(sessionId));
                        CuratorZookeeperClient.this.stateChanged(StateListener.RECONNECTED);
                    } else {
                        log.warn("New session created after old session lost, old session {}, new session {}.", Long.toHexString(lastSessionId), Long.toHexString(sessionId));
                        lastSessionId = sessionId;
                        CuratorZookeeperClient.this.stateChanged(StateListener.NEW_SESSION_CREATED);
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
