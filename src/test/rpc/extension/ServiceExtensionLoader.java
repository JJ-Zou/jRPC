package com.zjj.rpc.extension;

import com.zjj.rpc.common.utils.ReflectUtils;
import com.zjj.rpc.common.utils.StringUtils;
import com.zjj.rpc.config.annotation.SPI;
import com.zjj.rpc.config.configcenter.DynamicConfigurationFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class ServiceExtensionLoader<T> {
    private static final String DIRECTORY = "META-INF/jrpc/";

    // 缓存对应类型的ServiceExtensionLoader
    private static final ConcurrentMap<Class<?>, ServiceExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<>();
    // 缓存对应类型的实例
    private static final ConcurrentMap<Class<?>, Object> EXTENSION_INSTANCES = new ConcurrentHashMap<>();
    // 缓存对象的Holder
    private final ConcurrentMap<String, Holder<Object>> holderInstances = new ConcurrentHashMap<>();
    // 缓存bean名称的类型
    private final Holder<Map<String, Class<?>>> cachedClasses = new Holder<>();
    // 缓存类型对应的名称
    private final ConcurrentMap<Class<?>, String> cachedNames = new ConcurrentHashMap<>();
    private final Class<?> type;

    private String cachedDefaultName;

    private ServiceExtensionLoader(Class<?> type) {
        this.type = type;
    }

    public static <T> ServiceExtensionLoader<T> getExtensionLoader(Class<T> type) {
        ServiceExtensionLoader<T> loader = (ServiceExtensionLoader<T>) EXTENSION_LOADERS.get(type);
        if (loader == null) {
            loader = new ServiceExtensionLoader<>(type);
            EXTENSION_LOADERS.putIfAbsent(type, loader);
        }
        return loader;
    }

    public static void main(String[] args) {
        DynamicConfigurationFactory zookeeper = ServiceExtensionLoader
                .getExtensionLoader(DynamicConfigurationFactory.class)
                .getExtension("zookeeper");
        System.out.println(zookeeper.getClass());
        Iterable iterable = ServiceExtensionLoader
                .getExtensionLoader(Iterable.class)
                .getExtension("linkedList");
        System.out.println(iterable.getClass());
    }

    public String getExtensionName(T instance) {
        return getExtensionName(instance.getClass());
    }

    public String getExtensionName(Class<?> clazz) {
        getExtensionClasses();
        return cachedNames.get(clazz);
    }

    private boolean containsExtension(String name) {
        return getExtensionClasses().containsKey(name);
    }

    public T getOrDefaultExtension(String name) {
        return containsExtension(name) ? getExtension(name) : getDefaultExtension();
    }

    public T getDefaultExtension() {
        getExtensionClasses();
        if (cachedDefaultName == null || cachedDefaultName.isEmpty() || cachedDefaultName.equals("true")) {
            return null;
        }
        return getExtension(cachedDefaultName);
    }

    public T getExtension(String name) {
        if ("true".equals(name)) {
            return getDefaultExtension();
        }
        final Holder<Object> objectHolder = getOrDefault(name);
        Object instance = objectHolder.get();
        if (instance == null) {
            synchronized (objectHolder) {
                instance = objectHolder.get();
                if (instance == null) {
                    instance = createExtension(name);
                    objectHolder.set(instance);
                }
            }
        }
        return (T) instance;
    }

    private T createExtension(String name) {
        Class<?> clazz = getExtensionClasses().get(name);
        try {

            Object instance = EXTENSION_INSTANCES.get(clazz);
            if (instance == null) {
                instance = clazz.newInstance();
                EXTENSION_INSTANCES.putIfAbsent(clazz, instance);
            }

            return (T) instance;
        } catch (IllegalAccessException | InstantiationException e) {
            throw new IllegalStateException("Exception occurred where create extension class" + clazz + " instance {} " + name + ".", e);
        }
    }

    private void injectExtension(T instance) {
        for (Method method : instance.getClass().getMethods()) {
            if (ReflectUtils.isSetter(method)) {
                continue;
            }
            try {
                Class<?> parameterType = method.getParameterTypes()[0];
                String parameterName = StringUtils.calculateAttributeFromGetter(parameterType.getName());
                T object = getExtension(parameterName);
                method.invoke(instance, object);
            } catch (IllegalAccessException | InvocationTargetException e) {
                log.error("Failed to inject via method {} of interface {}: {}.", method.getName(), type.getName(), e.getMessage(), e);
            }
        }
    }

    private Map<String, Class<?>> getExtensionClasses() {
        Map<String, Class<?>> classMap = cachedClasses.get();
        if (classMap == null) {
            synchronized (cachedClasses) {
                classMap = cachedClasses.get();
                if (classMap == null) {
                    classMap = loadExtensionClasses();
                    cachedClasses.set(classMap);
                }
            }
        }
        return classMap;
    }

    private Map<String, Class<?>> loadExtensionClasses() {
        cacheDefaultExtensionName();
        Map<String, Class<?>> extensionClasses = new HashMap<>();
        loadDirectory(extensionClasses, type.getName());
        return extensionClasses;
    }

    private void loadDirectory(Map<String, Class<?>> extensionClasses, String type) {
        String fileName = DIRECTORY + type;
        try {
            ClassLoader classLoader = findClassLoader();
            Enumeration<URL> resources;
            if (classLoader != null) {
                resources = classLoader.getResources(fileName);
            } else {
                resources = ClassLoader.getSystemResources(fileName);
            }
            while (resources.hasMoreElements()) {
                loadResource(extensionClasses, classLoader, resources.nextElement());
            }
        } catch (IOException e) {
            log.error("Exception occurred when loading extension class {} where in file {}.", type, fileName, e);
        }
    }

    private void loadResource(Map<String, Class<?>> extensionClasses, ClassLoader classLoader, URL resourceURl) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceURl.openStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("##")) {
                    continue;
                }
                String[] split = line.split("=");
                String key = split[0].trim();
                String value = split[1].trim();
                loadClass(extensionClasses, classLoader, value, key);
            }
        } catch (IOException | ClassNotFoundException e) {
            log.error("Exception occurred when loading extension class {} where in file {}.", type, resourceURl, e);
        }
    }

    private void loadClass(Map<String, Class<?>> extensionClasses, ClassLoader classLoader, String className, String key) throws ClassNotFoundException {
        Class<?> clazz = Class.forName(className, true, classLoader);
        cacheName(clazz, key);
        extensionClasses.putIfAbsent(key, clazz);
    }

    private ClassLoader findClassLoader() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = ServiceExtensionLoader.class.getClassLoader();
        }
        return classLoader;
    }

    private Holder<Object> getOrDefault(String name) {
        Holder<Object> objectHolder = holderInstances.get(name);
        if (objectHolder == null) {
            objectHolder = new Holder<>();
            holderInstances.putIfAbsent(name, objectHolder);
        }
        return objectHolder;
    }

    private void cacheDefaultExtensionName() {
        SPI defaultAnnotation = type.getAnnotation(SPI.class);
        if (defaultAnnotation == null) {
            return;
        }
        cachedDefaultName = defaultAnnotation.value();
    }

    private void cacheName(Class<?> clazz, String name) {
        cachedNames.putIfAbsent(clazz, name);
    }
}
