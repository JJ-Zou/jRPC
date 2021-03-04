package com.zjj.registry.support;

import com.zjj.common.JRpcURL;
import com.zjj.common.JRpcURLParamType;
import com.zjj.registry.NotifyListener;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

@Slf4j
public abstract class FailbackRegistry extends AbstractRegistry {
    private final Set<JRpcURL> failedRegistered = ConcurrentHashMap.newKeySet();
    private final Set<JRpcURL> failedUnregistered = ConcurrentHashMap.newKeySet();
    private final ConcurrentMap<JRpcURL, Set<NotifyListener>> failedSubscribed = new ConcurrentHashMap<>();
    private final ConcurrentMap<JRpcURL, Set<NotifyListener>> failedUnsubscribed = new ConcurrentHashMap<>();

    private static final ScheduledExecutorService RETRY_EXECUTOR = Executors.newSingleThreadScheduledExecutor();

    protected FailbackRegistry(JRpcURL url) {
        super(url);
        int retryPeriod = url.getParameter(JRpcURLParamType.registryRetryPeriod.getName(), JRpcURLParamType.registryRetryPeriod.getIntValue());
        RETRY_EXECUTOR.scheduleAtFixedRate(() -> {
            try {
                retry();
            } catch (Exception e) {
                log.warn("[{}]: occur an exception when retry in failback registry.", registryClassName, e);
            }
        }, retryPeriod, retryPeriod, TimeUnit.MILLISECONDS);
    }

    @Override
    public void register(JRpcURL url) {
        failedRegistered.remove(url);
        failedUnregistered.remove(url);
        try {
            super.register(url);
        } catch (Exception e) {
            log.warn("[{}]: register URL ({}) occur an exception, put it into failedRegistered.", registryClassName, url, e);
            failedRegistered.add(url);
        }
    }

    @Override
    public void unregister(JRpcURL url) {
        failedRegistered.remove(url);
        failedUnregistered.remove(url);
        try {
            super.unregister(url);
        } catch (Exception e) {
            log.warn("[{}]: unregister URL ({}) occur an exception, put it into failedUnregistered.", registryClassName, url, e);
            failedUnregistered.add(url);
        }
    }

    @Override
    public void subscribe(JRpcURL url, NotifyListener listener) {
        removeFailSubAndUnsub(url, listener);
        try {
            super.subscribe(url, listener);
        } catch (Exception e) {
            List<JRpcURL> cachedUrls = getCachedUrls(url);
            if (!cachedUrls.isEmpty()) {
                listener.notify(url, cachedUrls);
            }
            log.warn("[{}]: subscribe URL ({}) with {} occur an exception, put it into failedSubscribed.", registryClassName, url, listener, e);
            failedSubscribed.computeIfAbsent(url, set -> ConcurrentHashMap.newKeySet()).add(listener);
        }
    }

    @Override
    public void unsubscribe(JRpcURL url, NotifyListener listener) {
        removeFailSubAndUnsub(url, listener);
        try {
            super.unsubscribe(url, listener);
        } catch (Exception e) {
            log.warn("[{}]: unsubscribe URL ({}) with {} occur an exception, put it into failedUnsubscribed.", registryClassName, url, listener, e);
            failedUnsubscribed.computeIfAbsent(url, set -> ConcurrentHashMap.newKeySet()).add(listener);
        }
    }

    @Override
    public List<JRpcURL> discover(JRpcURL url) {
        try {
            return super.discover(url);
        } catch (Exception e) {
            log.warn("[{}]: fail to discover URL ({}).", registryClassName, url, e);
            return Collections.emptyList();
        }
    }

    private void removeFailSubAndUnsub(JRpcURL url, NotifyListener listener) {
        Set<NotifyListener> listeners = failedSubscribed.get(url);
        if (listeners != null) {
            listeners.remove(listener);
        }
        listeners = failedUnsubscribed.get(url);
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    private void retry() {
        retryFailedRegistered();
        retryFailedUnregistered();
        retryFailedSubscribed();
        retryFailedUnsubscribed();
    }

    private void retryFailedRegistered() {
        failedRegistered.forEach(url -> {
            try {
                log.info("[{}]: Try to retry register URL ({}).", registryClassName, url);
                register(url);
            } catch (Exception e) {
                log.warn("[{}]: Fail to retry register URL ({}), we will retry later.", registryClassName, url, e);
            }
        });
    }

    private void retryFailedUnregistered() {
        failedUnregistered.forEach(url -> {
            try {
                log.info("[{}]: Try to retry unregister URL ({}).", registryClassName, url);
                unregister(url);
            } catch (Exception e) {
                log.warn("[{}]: Fail to retry unregister URL ({}), we will retry later.", registryClassName, url, e);
            }
        });
    }

    private void retryFailedSubscribed() {
        failedSubscribed.forEach((url, notifyListeners) -> notifyListeners.forEach(listener -> {
            try {
                log.info("[{}]: Try to retry subscribe URL ({}) with {}.", registryClassName, url, listener);
                subscribe(url, listener);
            } catch (Exception e) {
                log.warn("[{}]: Fail to retry subscribe URL ({}) with {}, we will retry later.", registryClassName, url, listener, e);
            }
        }));
    }

    private void retryFailedUnsubscribed() {
        failedSubscribed.forEach((url, notifyListeners) -> notifyListeners.forEach(listener -> {
            try {
                log.info("[{}]: Try to retry unsubscribe URL ({}) with {}.", registryClassName, url, listener);
                unsubscribe(url, listener);
            } catch (Exception e) {
                log.warn("[{}]: Fail to retry unsubscribe URL ({}) with {}, we will retry later.", registryClassName, url, listener, e);
            }
        }));
    }
}
