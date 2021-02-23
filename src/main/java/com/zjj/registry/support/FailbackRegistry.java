package com.zjj.registry.support;

import com.zjj.common.timer.HashedWheelTimer;
import com.zjj.common.utils.NameThreadFactory;
import com.zjj.common.URL;
import com.zjj.common.utils.CollectionUtils;
import com.zjj.registry.NotifyListener;
import com.zjj.registry.retry.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.zjj.registry.Constants.*;

@Slf4j
public abstract class FailbackRegistry extends AbstractRegistry {

    // 重试任务
    private final ConcurrentMap<URL, FailedRegisteredTask> failedRegistered = new ConcurrentHashMap<>();
    private final ConcurrentMap<URL, FailedUnregisteredTask> failedUnregistered = new ConcurrentHashMap<>();
    private final ConcurrentMap<Holder, FailedSubscribedTask> failedSubscribed = new ConcurrentHashMap<>();
    private final ConcurrentMap<Holder, FailedUnsubscribedTask> failedUnsubscribed = new ConcurrentHashMap<>();
    private final ConcurrentMap<Holder, FailedNotifiedTask> failedNotified = new ConcurrentHashMap<>();

    private final int retryPeriod;
    private final HashedWheelTimer retryTimer;

    public FailbackRegistry(URL url) {
        super(url);
        this.retryPeriod = url.getParameter(REGISTRY_RETRY_PERIOD_KEY, DEFAULT_REGISTRY_RETRY_PERIOD);
        this.retryTimer = new HashedWheelTimer(new NameThreadFactory(DUBBO_REGISTRY_RETRY_TIMER_KEY, true));
    }

    public void removeFailedRegisteredTask(URL url) {
        failedRegistered.remove(url);
    }

    public void removeFailedUnregisteredTask(URL url) {
        failedUnregistered.remove(url);
    }

    public void removeFailedSubscribedTask(URL url, NotifyListener listener) {
        Holder holder = new Holder(url, listener);
        failedSubscribed.remove(holder);
    }

    public void removeFailedUnsubscribedTask(URL url, NotifyListener listener) {
        Holder holder = new Holder(url, listener);
        failedUnsubscribed.remove(holder);
    }

    private void addFailedRegistered(URL url) {
        FailedRegisteredTask old = failedRegistered.get(url);
        if (old != null) {
            return;
        }
        FailedRegisteredTask newTask = new FailedRegisteredTask(url, this);
        old = failedRegistered.putIfAbsent(url, newTask);
        if (old == null) {
//            retryTimer.newTimeout(newTask, retryPeriod, TimeUnit.MILLISECONDS);
        }
    }

    private void removeFailedRegistered(URL url) {
        FailedRegisteredTask remove = failedRegistered.remove(url);
        if (remove != null) {
            remove.cancel();
        }
    }

    private void addFailedUnregistered(URL url) {
        FailedUnregisteredTask old = failedUnregistered.get(url);
        if (old != null) {
            return;
        }
        FailedUnregisteredTask newTask = new FailedUnregisteredTask(url, this);
        old = failedUnregistered.putIfAbsent(url, newTask);
        if (old == null) {
//            retryTimer.newTimeout(newTask, retryPeriod, TimeUnit.MICROSECONDS);
        }
    }


    private void removeFailedUnregistered(URL url) {
        FailedUnregisteredTask remove = failedUnregistered.remove(url);
        if (remove != null) {
            remove.cancel();
        }
    }

    protected void addFailedSubscribed(URL url, NotifyListener listener) {
        Holder holder = new Holder(url, listener);
        FailedSubscribedTask old = failedSubscribed.get(holder);
        if (old != null) {
            return;
        }
        FailedSubscribedTask newTask = new FailedSubscribedTask(url, this, listener);
        old = failedSubscribed.putIfAbsent(holder, newTask);
        if (old == null) {
//            retryTimer.newTimeout(newTask, retryPeriod, TimeUnit.MICROSECONDS);
        }
    }

    private void removeFailedSubscribed(URL url, NotifyListener listener) {
        Holder holder = new Holder(url, listener);
        FailedSubscribedTask remove = failedSubscribed.remove(holder);
        if (remove != null) {
            remove.cancel();
        }
        removeFailedUnsubscribed(url, listener);
        removeFailedNotified(url, listener);
    }


    private void addFailedUnsubscribed(URL url, NotifyListener listener) {
        Holder holder = new Holder(url, listener);
        FailedUnsubscribedTask old = failedUnsubscribed.get(holder);
        if (old != null) {
            return;
        }
        FailedUnsubscribedTask newTask = new FailedUnsubscribedTask(url, this, listener);
        old = failedUnsubscribed.putIfAbsent(holder, newTask);
        if (old == null) {
//            retryTimer.newTimeout(newTask, retryPeriod, TimeUnit.MICROSECONDS);
        }
    }

    private void removeFailedUnsubscribed(URL url, NotifyListener listener) {
        Holder holder = new Holder(url, listener);
        FailedUnsubscribedTask remove = failedUnsubscribed.remove(holder);
        if (remove != null) {
            remove.cancel();
        }
    }

    private void addFailedNotified(URL url, NotifyListener listener, List<URL> urls) {
        Holder holder = new Holder(url, listener);
        FailedNotifiedTask newTask = new FailedNotifiedTask(url, listener);
        FailedNotifiedTask f = failedNotified.putIfAbsent(holder, newTask);
        newTask.addUrlToRetry(urls);
        if (f == null) {
//            retryTimer.newTimeout(newTask, retryPeriod, TimeUnit.MICROSECONDS);
        }
    }

    private void removeFailedNotified(URL url, NotifyListener listener) {
        Holder holder = new Holder(url, listener);
        FailedNotifiedTask remove = failedNotified.remove(holder);
        if (remove != null) {
            remove.cancel();
        }
    }

    @Override
    public void register(URL url) {
        if (!acceptable(url)) {
            log.debug("URL {} will not be registered to Registry. Registry {} does not accept service of this protocol type.",
                    url, url);
            return;
        }
        super.register(url);
        removeFailedRegistered(url);
        removeFailedUnregistered(url);
        try {
            doRegister(url);
        } catch (Exception e) {
            Throwable t = e;

            boolean check = getUrl().getParameter(CHECK_KEY, true)
                    && url.getParameter(CHECK_KEY, true)
                    && !CONSUMER_PROTOCOL.equals(url.getProtocol());
            boolean skipFailback = t instanceof SkipFailbackWrapperException;
            if (check || skipFailback) {
                if (skipFailback) {
                    t = t.getCause();
                }
                throw new IllegalStateException("Failed to register " + url + " to registry " + getUrl().getAddress() + ", cause: " + t.getMessage(), t);
            } else {
                log.error("Failed to register {}, waiting for retry, cause: {}", url, t.getMessage(), t);
            }
            addFailedRegistered(url);
        }
    }

    @Override
    public void reExportRegister(URL url) {
        if (!acceptable(url)) {
            log.debug("URL {} will not be registered to Registry. Registry {} does not accept service of this protocol type.",
                    url, url);
            return;
        }
        super.register(url);
        removeFailedRegistered(url);
        removeFailedUnregistered(url);
        try {
            doRegister(url);
        } catch (Exception e) {
            if (!(e instanceof SkipFailbackWrapperException)) {
                throw new IllegalStateException("Failed to register (re-export) " + url + " to registry " + getUrl().getAddress() + ", cause: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public void unregister(URL url) {
        super.unregister(url);
        removeFailedRegistered(url);
        removeFailedUnregistered(url);
        try {
            doUnregister(url);
        } catch (Exception e) {
            Throwable t = e;

            boolean check = getUrl().getParameter(CHECK_KEY, true)
                    && url.getParameter(CHECK_KEY, true)
                    && !CONSUMER_PROTOCOL.equals(url.getProtocol());
            boolean skipFailback = t instanceof SkipFailbackWrapperException;
            if (check || skipFailback) {
                if (skipFailback) {
                    t = t.getCause();
                }
                throw new IllegalStateException("Failed to unregister " + url + " to registry " + getUrl().getAddress() + ", cause: " + t.getMessage(), t);
            } else {
                log.error("Failed to unregister {}, waiting for retry, cause: {}", url, t.getMessage(), t);
            }
            addFailedUnregistered(url);
        }
    }

    @Override
    public void reExportUnregister(URL url) {
        super.unregister(url);
        removeFailedRegistered(url);
        removeFailedUnregistered(url);
        try {
            doUnregister(url);
        } catch (Exception e) {
            if (!(e instanceof SkipFailbackWrapperException)) {
                throw new IllegalStateException("Failed to unregister (re-export) " + url + " to registry " + getUrl().getAddress() + ", cause: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public void subscribe(URL url, NotifyListener listener) {
        super.subscribe(url, listener);
        removeFailedSubscribed(url, listener);
        try {
            doSubscribe(url, listener);
        } catch (Exception e) {
            Throwable t = e;
            List<URL> urls = getCacheUrls(url);
            if (CollectionUtils.isNotEmpty(urls)) {
                notify(url, listener, urls);
                log.error("Failed to subscribe {}, Using cached list: {} from cache file: {}, cause: {}", url, urls, t.getMessage(), t);
            } else {
                boolean check = getUrl().getParameter(CHECK_KEY, true)
                        && url.getParameter(CHECK_KEY, true)
                        && !CONSUMER_PROTOCOL.equals(url.getProtocol());
                boolean skipFailback = t instanceof SkipFailbackWrapperException;
                if (check || skipFailback) {
                    if (skipFailback) {
                        t = t.getCause();
                    }
                    throw new IllegalStateException("Failed to subscribe " + url + " to registry " + getUrl().getAddress() + ", cause: " + t.getMessage(), t);
                } else {
                    log.error("Failed to subscribe {}, waiting for retry, cause: {}", url, t.getMessage(), t);
                }
            }
            addFailedSubscribed(url, listener);
        }
    }

    @Override
    public void unsubscribe(URL url, NotifyListener listener) {
        super.unsubscribe(url, listener);
        removeFailedSubscribed(url, listener);
        try {
            doUnsubscribe(url, listener);
        } catch (Exception e) {
            Throwable t = e;

            boolean check = getUrl().getParameter(CHECK_KEY, true)
                    && url.getParameter(CHECK_KEY, true)
                    && !CONSUMER_PROTOCOL.equals(url.getProtocol());
            boolean skipFailback = t instanceof SkipFailbackWrapperException;
            if (check || skipFailback) {
                if (skipFailback) {
                    t = t.getCause();
                }
                throw new IllegalStateException("Failed to unsubscribe " + url + " to registry " + getUrl().getAddress() + ", cause: " + t.getMessage(), t);
            } else {
                log.error("Failed to unsubscribe {}, waiting for retry, cause: {}", url, t.getMessage(), t);
            }
            addFailedUnsubscribed(url, listener);
        }
    }


    @Override
    protected void notify(URL url, NotifyListener listener, List<URL> urls) {
        if (url == null) {
            throw new IllegalArgumentException("notify url == null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("notify listener == null");
        }
        try {
            doNotify(url, listener, urls);
        } catch (Exception e) {
            addFailedNotified(url, listener, urls);
            log.error("Failed to notify for subscribe {}, waiting for retry, cause: {}", url, e.getMessage(), e);
        }
    }

    protected void doNotify(URL url, NotifyListener listener, List<URL> urls) {
        super.notify(url, listener, urls);
    }

    @Override
    protected void recover() throws Exception {
        Set<URL> recoverRegistered = new HashSet<>(getRegistered());
        if (!recoverRegistered.isEmpty()) {
            log.debug("Recover register url {}", recoverRegistered);
            for (URL url : recoverRegistered) {
                addFailedRegistered(url);
            }
        }
        Map<URL, Set<NotifyListener>> recoverSubscribed = new HashMap<>(getSubscribed());
        if (!recoverSubscribed.isEmpty()) {
            log.debug("Recover subscribe url {}", recoverSubscribed.keySet());
            for (Map.Entry<URL, Set<NotifyListener>> entry : recoverSubscribed.entrySet()) {
                URL url = entry.getKey();
                for (NotifyListener listener : entry.getValue()) {
                    addFailedSubscribed(url, listener);
                }
            }
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        retryTimer.stop();
    }

    // 模板方法

    public abstract void doRegister(URL url);

    public abstract void doUnregister(URL url);

    public abstract void doSubscribe(URL url, NotifyListener listener);

    public abstract void doUnsubscribe(URL url, NotifyListener listener);

    static class Holder {
        private final URL url;
        private final NotifyListener listener;

        Holder(URL url, NotifyListener listener) {
            if (url == null || listener == null) {
                throw new IllegalArgumentException();
            }
            this.url = url;
            this.listener = listener;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Holder) {
                Holder h = (Holder) o;
                return this.url.equals(h.url) && this.listener.equals(h.listener);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return url.hashCode() + listener.hashCode();
        }
    }
}
