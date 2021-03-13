package com.zjj.config;

import com.zjj.common.JRpcURL;
import com.zjj.common.JRpcURLParamType;
import com.zjj.common.utils.ReflectUtils;
import com.zjj.exception.JRpcErrorMessage;
import com.zjj.exception.JRpcFrameworkException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Setter
@Getter
@Slf4j
public abstract class AbstractInterfaceConfig extends AbstractConfig {
    private static final long serialVersionUID = -7564123152873135129L;

    protected final Set<JRpcURL> registryUrls = ConcurrentHashMap.newKeySet();

    protected transient Map<String, String> parameters = new HashMap<>();
    protected transient List<RegistryConfig> registryConfigs;
    protected transient List<ProtocolConfig> protocolConfigs;
    protected transient List<MethodConfig> methodConfigs;

    protected String application;
    protected String module;
    protected String group;
    protected String version;
    protected String proxy;


    public void addRegistryConfig(RegistryConfig... registryConfigs) {
        this.registryConfigs.addAll(Arrays.asList(registryConfigs));
    }

    public void addProtocolConfig(ProtocolConfig... protocolConfigs) {
        this.protocolConfigs.addAll(Arrays.asList(protocolConfigs));
    }

    protected void checkRegistry() {
        convertRegistryConfigsToUrls();
        if (registryUrls.isEmpty()) {
            throw new IllegalStateException("Need at least one registry!");
        }
    }

    protected void checkProtocolConfigs() {
        if (CollectionUtils.isEmpty(protocolConfigs)) {
            throw new JRpcFrameworkException("check protocolConfigs not initialized.", JRpcErrorMessage.FRAMEWORK_INIT_ERROR);
        }
    }

    protected void checkInterface(Class<?> interfaceClass) {
        try {
            Class.forName(interfaceClass.getName(), true, ReflectUtils.getClassLoader());
            return;
        } catch (ClassNotFoundException e) {
            log.error("{} ClassNotFoundException", interfaceClass);
        }
        throw new JRpcFrameworkException("check interface " + interfaceClass + " illegal.", JRpcErrorMessage.FRAMEWORK_INIT_ERROR);
    }

    /**
     * 将 List<RegistryConfig> registryConfigs 解析并存入 Set<JRpcURL> registryUrls
     */
    private void convertRegistryConfigsToUrls() {
        if (CollectionUtils.isEmpty(registryConfigs)) {
            return;
        }
        registryConfigs.forEach(registryConfig -> {
            String protocol = registryConfig.getRegisterProtocol();
            String address = registryConfig.getAddress();
            Integer port = registryConfig.getPort();
            String path = registryConfig.getId();
            String[] addresses = JRpcURLParamType.COMMA_SPLIT_PATTERN.getPattern().split(address);
            Arrays.stream(addresses)
                    .forEach(a -> {
                        JRpcURL url;
                        if (!a.contains(JRpcURLParamType.COLON.getValue())) {
                            url = new JRpcURL(protocol, a, port, path);
                        } else {
                            String[] addr = JRpcURLParamType.COLON_SPLIT_PATTERN.getPattern().split(a);
                            url = new JRpcURL(protocol, addr[0], Integer.parseInt(addr[1]), path);
                        }
                        registryUrls.add(url);
                    });
        });
    }

    /**
     * 将 provider 的 interfaceClass 中暴露方法取出，方法参数信息存入 List<MethodConfig> methodConfigs
     *
     * @param interfaceClass export interface
     * @param methodConfigs  export method configs
     */
    protected void checkInterfaceAndMethods(Class<?> interfaceClass, List<MethodConfig> methodConfigs) {
        if (interfaceClass == null) {
            throw new IllegalStateException("interface not allow null!");
        }
        if (!interfaceClass.isInterface()) {
            throw new IllegalStateException("The interface class " + interfaceClass + " is not an interface!");
        }
        if (CollectionUtils.isEmpty(methodConfigs)) {
            return;
        }
        methodConfigs.forEach(methodConfig -> {
            Method method = Arrays.stream(interfaceClass.getMethods())
                    .filter(m -> m.getName().equals(methodConfig.getName()))
                    .findFirst()
                    .orElseThrow(() -> new JRpcFrameworkException("Cannot find method " + methodConfig.getName() + " in the interface " + interfaceClass.getName()));
            methodConfig.setArgumentTypes(ReflectUtils.getMethodSign(method));
        });
    }
}
