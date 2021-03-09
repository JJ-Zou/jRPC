package com.zjj.config;

import com.zjj.common.JRpcURL;
import com.zjj.common.JRpcURLParamType;
import com.zjj.common.utils.ReflectUtils;
import com.zjj.exception.JRpcFrameworkException;
import com.zjj.registry.RegistryService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Setter
@Getter
public abstract class AbstractInterfaceConfig extends AbstractConfig {
    private static final long serialVersionUID = -7564123152873135129L;

    protected transient Set<JRpcURL> registryUrls;
    protected transient List<RegistryConfig> registryConfigs;
    protected transient List<ProtocolConfig> protocolConfigs;
    protected String application;
    protected String module;
    protected String group;
    protected String version;
    protected String proxy;


    protected void checkRegistry() {
        convertRegistryConfigsToUrls();
        if (registryUrls.isEmpty()) {
            throw new IllegalStateException("Need at least one registry!");
        }
    }

    /**
     * 将 List<RegistryConfig> registryConfigs 解析并存入 Set<JRpcURL> registryUrls
     */
    private void convertRegistryConfigsToUrls() {
        if (CollectionUtils.isEmpty(registryConfigs)) {
            return;
        }
        registryUrls = new HashSet<>();
        registryConfigs.forEach(registryConfig -> {
            String protocol = registryConfig.getRegisterProtocol();
            String address = registryConfig.getAddress();
            Integer port = registryConfig.getPort();
            String path = RegistryService.class.getName();
            String[] addresses = JRpcURLParamType.commaSplitPattern.getPattern().split(address);
            Arrays.stream(addresses)
                    .forEach(a -> {
                        JRpcURL url;
                        if (!a.contains(JRpcURLParamType.colon.getValue())) {
                            url = new JRpcURL(protocol, a, port, path);
                        } else {
                            String[] addr = JRpcURLParamType.colonSplitPattern.getPattern().split(a);
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
