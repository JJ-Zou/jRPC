package com.zjj.extension;

import com.zjj.common.utils.ReflectUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class ExtensionLoader<T> {

    private static final String PREFIX = "META-INF/jrpc/";

    private static final ConcurrentMap<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<>();

    private static final ConcurrentMap<Class<?>, Object> EXTENSION_CLASS_INSTANCES = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, Object> extensionNameInstances = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, Class<?>> extensionClasses = new ConcurrentHashMap<>();

    private final Class<T> type;
    private final ClassLoader classLoader;

    private String cachedDefaultKey;

    private ExtensionLoader(Class<T> type) {
        this.type = type;
        this.classLoader = Thread.currentThread().getContextClassLoader();
    }

    public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> type) {
        return (ExtensionLoader<T>) EXTENSION_LOADERS.computeIfAbsent(type, e -> new ExtensionLoader<>(type));
    }

    public T getOrDefaultExtension(String name) {
        if (containsExtension(name)) {
            getExtension(name);
        }
        return getDefaultExtension();
    }

    private boolean containsExtension(String name) {
        return extensionNameInstances.containsKey(name);
    }

    public T getDefaultExtension() {
        loadExtensionClasses();
        if (cachedDefaultKey == null) {
            return null;
        }
        return getExtension(cachedDefaultKey);
    }

    public T getExtension(String name) {
        if (name == null) {
            return null;
        }
        Object instance = extensionNameInstances.computeIfAbsent(name, o -> createExtension(name));
        return (T) instance;
    }

    private T createExtension(String name) {
        if (!extensionClasses.containsKey(name)) {
            loadExtensionClasses();
        }
        Class<?> clazz = extensionClasses.get(name);
        if (clazz == null) {
            throw new IllegalStateException("Cannot find instance of " + name);
        }
        T instance = (T) EXTENSION_CLASS_INSTANCES.computeIfAbsent(clazz, o -> constructAndInjectExtension(clazz));
        return instance;
    }

    private T constructAndInjectExtension(Class<?> clazz) {
        try {
            T instance = (T) clazz.newInstance();
            injectExtension(instance);
            return instance;
        } catch (Exception e) {
            throw new IllegalStateException("We cannot create an instance of class " + clazz, e);
        }
    }

    private void injectExtension(T instance) {
        Arrays.stream(instance.getClass().getMethods())
                .filter(ReflectUtils::isSetter)
                .forEach(method -> {
                    try {
                        Class<?> parameterType = method.getParameterTypes()[0];
                        Object extension = getExtensionLoader(parameterType).getDefaultExtension();
                        if (extension != null) {
                            method.invoke(instance, extension);
                        }
                    } catch (Exception e) {
                        log.error("Cannot use method {} inject filed into instance {}.", method, instance, e);
                    }
                });
    }

    private void loadExtensionClasses() {
        cacheDefaultKey();
        loadDirectory(type.getName());
    }

    private void cacheDefaultKey() {
        SPI annotation = type.getAnnotation(SPI.class);
        if (annotation == null || StringUtils.isBlank(annotation.value())) {
            String simpleName = type.getSimpleName();
            this.cachedDefaultKey = simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);
            return;
        }
        this.cachedDefaultKey = annotation.value();
    }

    private void loadDirectory(String name) {
        String fileName = PREFIX + name;
        Enumeration<URL> urls;
        try {
            if (classLoader != null) {
                urls = classLoader.getResources(fileName);
            } else {
                urls = ClassLoader.getSystemResources(fileName);
            }
            while (urls.hasMoreElements()) {
                loadResource(urls.nextElement());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadResource(URL url) {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] entry = line.split("=");
                String key = entry[0].trim();
                String className = entry[1].trim();
                loadClass(key, className);
            }
        } catch (Exception e) {
            log.error("We load resource {} fail while getting instance of type {}.", url, type);
        }
    }

    private void loadClass(String key, String className) {
        try {
            Class<?> clazz;
            if (classLoader != null) {
                clazz = Class.forName(className, true, classLoader);
            } else {
                clazz = Class.forName(className);
            }
            extensionClasses.putIfAbsent(key, clazz);
        } catch (Exception e) {
            log.error("We load class {} fail while getting instance of type {}.", key, className);
        }
    }
}
