package com.zjj.registry.support;

import com.zjj.registry.Constants;
import com.zjj.registry.Registry;
import com.zjj.common.URL;
import com.zjj.common.utils.CollectionUtils;
import com.zjj.common.utils.ConcurrentHashSet;
import com.zjj.common.utils.StringUtils;
import com.zjj.common.utils.UrlUtils;
import com.zjj.registry.NotifyListener;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

import static com.zjj.common.constants.CommonConstants.*;
import static com.zjj.common.constants.RegistryConstants.*;


@Slf4j
public abstract class AbstractRegistry implements Registry {

    // url分隔符正则表达式，匹配所有空白字符
    private static final String URL_SPLIT = "\\s+";
    private final Properties properties = new Properties();
    private final Set<URL> registered = new ConcurrentHashSet<>();
    private final ConcurrentMap<URL, Set<NotifyListener>> subscribed = new ConcurrentHashMap<>();
    private final ConcurrentMap<URL, Map<String, List<URL>>> notified = new ConcurrentHashMap<>();
    private URL registryUrl;


    public AbstractRegistry(URL url) {
        setUrl(url);
        if (url.getParameter(Constants.REGISTRY_LOCAL_FILE_CACHE_ENABLED, true)) {
            // 缓存url到本地文件
        }
    }

    public Set<URL> getRegistered() {
        return Collections.unmodifiableSet(registered);
    }

    public Map<URL, Set<NotifyListener>> getSubscribed() {
        return Collections.unmodifiableMap(subscribed);
    }

    public Map<URL, Map<String, List<URL>>> getNotified() {
        return Collections.unmodifiableMap(notified);
    }

    @Override
    public URL getUrl() {
        return registryUrl;
    }

    protected void setUrl(URL url) {
        if (url == null) {
            throw new IllegalArgumentException("registry url == null.");
        }
        this.registryUrl = url;
    }


    @Override
    public void register(URL url) {
        if (url == null) {
            throw new IllegalArgumentException("register url == null.");
        }
        log.debug("Register: {}", url);
        registered.add(url);
    }


    @Override
    public void unregister(URL url) {
        if (url == null) {
            throw new IllegalArgumentException("unregister url == null.");
        }
        log.debug("Unregister: {}", url);
        registered.remove(url);
    }

    @Override
    public void subscribe(URL url, NotifyListener listener) {
        if (url == null) {
            throw new IllegalArgumentException("subscribe url == null.");
        }
        if (listener == null) {
            throw new IllegalArgumentException("subscribe listener == null.");
        }
        log.debug("Subscribe: {}", url);
        Set<NotifyListener> listeners = subscribed.computeIfAbsent(url, k -> new ConcurrentHashSet<>());
        listeners.add(listener);
    }

    @Override
    public void unsubscribe(URL url, NotifyListener listener) {
        if (url == null) {
            throw new IllegalArgumentException("unsubscribe url == null.");
        }
        if (listener == null) {
            throw new IllegalArgumentException("unsubscribe listener == null.");
        }

        log.debug("Unsubscribe: {}", url);
        Set<NotifyListener> listeners = subscribed.get(url);
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    protected void recover() throws Exception {
        Set<URL> recoverRegistered = new HashSet<>(getRegistered());
        if (!recoverRegistered.isEmpty()) {
            log.debug("Recover register url {}", recoverRegistered);
            for (URL url : recoverRegistered) {
                register(url);
            }
        }
        Map<URL, Set<NotifyListener>> recoverSubscribed = new HashMap<>(getSubscribed());
        if (!recoverSubscribed.isEmpty()) {
            log.debug("Recover subscribe url {}", recoverSubscribed.keySet());
            for (Map.Entry<URL, Set<NotifyListener>> entry : recoverSubscribed.entrySet()) {
                URL url = entry.getKey();
                for (NotifyListener listener : entry.getValue()) {
                    subscribe(url, listener);
                }
            }
        }
    }

    protected void notify(List<URL> urls) {
        if (CollectionUtils.isEmpty(urls)) {
            return;
        }
        for (Map.Entry<URL, Set<NotifyListener>> entry : getSubscribed().entrySet()) {
            URL url = entry.getKey();
            if (!UrlUtils.isMatch(url, urls.get(0))) {
                continue;
            }
            Set<NotifyListener> listeners = entry.getValue();
            if (listeners != null) {
                for (NotifyListener listener : listeners) {
                    try {
                        notify(url, listener, urls);
                    } catch (Throwable t) {
                        log.error("Failed to notify registry event, urls: {}, cause: {}", urls, t.getMessage(), t);
                    }
                }
            }
        }
    }

    protected void notify(URL url, NotifyListener listener, List<URL> urls) {
        if (url == null) {
            throw new IllegalArgumentException("notify url == null.");
        }

        if (listener == null) {
            throw new IllegalArgumentException("notify listener == null.");
        }
        if (CollectionUtils.isEmpty(urls) && !ANY_VALUE.equals(url.getServiceInterface())) {
            log.warn("Ignore empty notify urls for subscribe url {}", url);
            return;
        }
        log.debug("Notify urls for subscribe url {}, urls: {}", url, urls);
        Map<String, List<URL>> result = new HashMap<>();
        for (URL u : urls) {
            if (UrlUtils.isMatch(url, u)) {
                String category = u.getParameter(CATEGORY_KEY, DEFAULT_CATEGORY);
                List<URL> categoryList = result.computeIfAbsent(category, k -> new ArrayList<>());
                categoryList.add(u);
            }
        }
        if (result.size() == 0) {
            return;
        }
        Map<String, List<URL>> categoryNotified = notified.computeIfAbsent(url, u -> new ConcurrentHashMap<>());
        for (Map.Entry<String, List<URL>> entry : result.entrySet()) {
            String category = entry.getKey();
            List<URL> categoryList = entry.getValue();
            categoryNotified.put(category, categoryList);
            listener.notify(categoryList);
            // 更新缓存文件
        }
    }

    public List<URL> getCacheUrls(URL url) {
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            if (StringUtils.isNotEmpty(key)
                    && key.equals(url.getServiceKey())
                    && (Character.isLetter(key.charAt(0)) || key.charAt(0) == '_')
                    && StringUtils.isNotEmpty(value)) {
                String[] arr = value.trim().split(URL_SPLIT);
                List<URL> urls = new ArrayList<>();
                for (String s : arr) {
                    urls.add(URL.valueOf(s));
                }
                return urls;
            }
        }
        return null;
    }

    @Override
    public List<URL> lookup(URL url) {
        List<URL> result = new ArrayList<>();
        Map<String, List<URL>> notifiedUrls = getNotified().get(url);
        if (CollectionUtils.isNotEmptyMap(notifiedUrls)) {
            for (List<URL> urls : notifiedUrls.values()) {
                for (URL u : urls) {
                    if (!EMPTY_PROTOCOL.equals(u.getProtocol())) {
                        result.add(u);
                    }
                }
            }
        } else {
            AtomicReference<List<URL>> reference = new AtomicReference<>();
            NotifyListener listener = reference::set;
            subscribe(url, listener);
            List<URL> urls = reference.get();
            if (CollectionUtils.isNotEmpty(urls)) {
                for (URL u : urls) {
                    if (!EMPTY_PROTOCOL.equals(u.getProtocol())) {
                        result.add(u);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public void destroy() {
        log.debug("Destroy registry: {}", getUrl());
        Set<URL> destroyRegistered = new HashSet<>(getRegistered());
        if (!destroyRegistered.isEmpty()) {
            for (URL url : new HashSet<>(getRegistered())) {
                if (url.getParameter(DYNAMIC_KEY, true)) {
                    try {
                        unregister(url);
                        log.debug("Destroy unregister url {}", url);
                    } catch (Throwable t) {
                        log.warn("Failed to unregister url {} to registry {} on destroy, cause: {}", url, getUrl(), t.getMessage(), t);
                    }
                }
            }
        }
        HashMap<URL, Set<NotifyListener>> destroySubscribed = new HashMap<>(getSubscribed());
        if (!destroySubscribed.isEmpty()) {
            for (Map.Entry<URL, Set<NotifyListener>> entry : destroySubscribed.entrySet()) {
                URL url = entry.getKey();
                for (NotifyListener listener : entry.getValue()) {
                    try {
                        unsubscribe(url, listener);
                        log.debug("Destroy unsubscribe url {}", url);
                    } catch (Throwable t) {
                        log.warn("Failed to unsubscribe url {} to registry {} on destroy, cause: {}", url, getUrl(), t.getMessage(), t);
                    }
                }
            }
        }
        AbstractRegistryFactory.removeDestroyedRegistry(this);
    }

    protected boolean acceptable(URL urlToRegister) {
        String pattern = registryUrl.getParameter(ACCEPTS_KEY);
        if (StringUtils.isEmpty(pattern)) {
            return true;
        }
        return Arrays.stream(COMMA_SPLIT_PATTERN.split(pattern))
                .anyMatch(p -> p.equalsIgnoreCase(urlToRegister.getProtocol()));
    }

}
