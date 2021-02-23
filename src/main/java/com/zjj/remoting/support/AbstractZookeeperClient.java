package com.zjj.remoting.support;

import com.zjj.common.URL;
import com.zjj.common.utils.ConcurrentHashSet;
import com.zjj.registry.zookeeper.ZookeeperClient;
import com.zjj.remoting.ChildListener;
import com.zjj.remoting.DataListener;
import com.zjj.remoting.StateListener;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executor;

@Slf4j
public abstract class AbstractZookeeperClient<TargetDataListener, TargetChildListener> implements ZookeeperClient {

    protected int DEFAULT_CONNECTION_TIMEOUT_MS = 5 * 1000;
    protected int DEFAULT_SESSION_TIMEOUT_MS = 60 * 1000;

    private final URL url;

    private final Set<StateListener> stateListeners = new CopyOnWriteArraySet<>();

    private final ConcurrentMap<String, ConcurrentMap<ChildListener, TargetChildListener>> childListeners = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, ConcurrentMap<DataListener, TargetDataListener>> listeners = new ConcurrentHashMap<>();

    private volatile boolean closed = false;

    private final Set<String> persistentExistNodePath = new ConcurrentHashSet<>();

    public AbstractZookeeperClient(URL url) {
        this.url = url;
    }

    @Override
    public URL getUrl() {
        return url;
    }

    @Override
    public void delete(String path) {
        persistentExistNodePath.remove(path);
        deletePath(path);
    }

    @Override
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

    @Override
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

    @Override
    public String getContent(String path) {
        if (!checkExists(path)) {
            return null;
        }
        return doGetContent(path);
    }

    @Override
    public void addStateListener(StateListener listener) {
        stateListeners.add(listener);
    }

    @Override
    public void removeStateListener(StateListener listener) {
        stateListeners.remove(listener);
    }

    public Set<StateListener> getSessionListeners() {
        return stateListeners;
    }

    @Override
    public List<String> addChildrenListener(String path, ChildListener listener) {
        TargetChildListener targetChildListener = childListeners
                .computeIfAbsent(path, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(listener, k -> createTargetChildListener(path, k));
        return addTargetChildListener(path, targetChildListener);
    }

    @Override
    public void removeChildrenListener(String path, ChildListener listener) {
        ConcurrentMap<ChildListener, TargetChildListener> targetChildListeners = childListeners.get(path);
        if (targetChildListeners != null) {
            TargetChildListener removed = targetChildListeners.remove(listener);
            if (removed != null) {
                removeTargetChildListener(path, removed);
            }
        }
    }

    @Override
    public void addDataListener(String path, DataListener listener) {
        this.addDataListener(path, listener, null);
    }

    @Override
    public void addDataListener(String path, DataListener listener, Executor executor) {
        TargetDataListener targetDataListener = listeners
                .computeIfAbsent(path, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(listener, k -> createTargetDataListener(path, k));
        addTargetDataListener(path, targetDataListener, executor);
    }

    @Override
    public void removeDataListener(String path, DataListener listener) {
        ConcurrentMap<DataListener, TargetDataListener> targetDataListeners = listeners.get(path);
        if (targetDataListeners != null) {
            TargetDataListener removed = targetDataListeners.remove(listener);
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

    @Override
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

    protected abstract void doClose();

    protected abstract void createPersistent(String path);

    protected abstract void createPersistent(String path, String data);

    protected abstract void createEphemeral(String path);

    protected abstract void createEphemeral(String path, String data);

    protected abstract boolean checkExists(String path);

    protected abstract String doGetContent(String path);

    protected abstract void deletePath(String path);

    protected abstract TargetDataListener createTargetDataListener(String path, DataListener listener);

    protected abstract void addTargetDataListener(String path, TargetDataListener listener);

    protected abstract void addTargetDataListener(String path, TargetDataListener listener, Executor executor);

    protected abstract void removeTargetDataListener(String path, TargetDataListener listener);

    protected abstract TargetChildListener createTargetChildListener(String path, ChildListener listener);

    protected abstract List<String> addTargetChildListener(String path, TargetChildListener listener);

    protected abstract void removeTargetChildListener(String path, TargetChildListener listener);
}
