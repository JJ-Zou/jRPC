package com.zjj.config;

import com.zjj.clutter.Clutter;
import com.zjj.clutter.clutter.ClutterNotify;
import com.zjj.common.JRpcURL;
import com.zjj.common.JRpcURLParamType;
import com.zjj.common.utils.NetUtils;
import com.zjj.extension.ExtensionLoader;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReferenceConfig<T> extends AbstractReferenceConfig {
    private static final long serialVersionUID = -8529195602996641811L;

    private static final ConfigHandler CONFIG_HANDLER = ExtensionLoader.getExtensionLoader(ConfigHandler.class).getDefaultExtension();

    private transient volatile boolean initialized;

    private transient volatile boolean destroyed;
    private T ref;
    private String directConnectStr;

    private Class<T> interfaceClass;

    private String serverInterface;

    protected List<MethodConfig> methodConfigs;

    private final List<ClutterNotify<T>> clutterNotifies = new ArrayList<>();

    public T getRef() {
        if (ref == null) {
            init();
        }
        return ref;
    }

    public void init() {
        if (initialized) {
            return;
        }
        checkInterface(interfaceClass);
        checkProtocolConfigs();
        checkRegistry();
        checkInterfaceAndMethods(interfaceClass, methodConfigs);
        String localIp = NetUtils.getLocalHostString();
        List<Clutter<T>> clutters = protocolConfigs.stream().map(protocolConfig -> {
            Map<String, String> params = new HashMap<>();
            params.put(JRpcURLParamType.nodeType.getName(), JRpcURLParamType.referer.getValue());
            params.put(JRpcURLParamType.version.getName(), JRpcURLParamType.version.getValue());
            params.put(JRpcURLParamType.refreshTimestamp.getName(), String.valueOf(System.currentTimeMillis()));
            refreshConfigs(params, protocolConfig, this);
            refreshMethodConfigs(params, methodConfigs);
            String path = StringUtils.isBlank(serverInterface) ? interfaceClass.getName() : serverInterface;
            JRpcURL refUrl = new JRpcURL(protocolConfig.getName(), localIp, path, params);
            ClutterNotify<T> clutterNotify = createClutterNotify(refUrl);
            clutterNotifies.add(clutterNotify);
            return clutterNotify.getClutter();
        }).collect(Collectors.toList());
        ref = CONFIG_HANDLER.refer(interfaceClass, clutters);
        initialized = true;
    }

    private ClutterNotify<T> createClutterNotify(JRpcURL refUrl) {
        if (StringUtils.isNotBlank(directConnectStr)) {

        }
        return CONFIG_HANDLER.getClutterNotify(interfaceClass, registryUrls, refUrl);
    }

    public void setInterfaceClass(Class<T> interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

    public void setMethodConfigs(List<MethodConfig> methodConfigs) {
        this.methodConfigs = methodConfigs;
    }

    public List<MethodConfig> getMethodConfigs() {
        return methodConfigs;
    }

    public List<ClutterNotify<T>> getClutterNotifies() {
        return clutterNotifies;
    }

    public Class<T> getInterfaceClass() {
        return interfaceClass;
    }

    public String getDirectConnectStr() {
        return directConnectStr;
    }

    public static ConfigHandler getConfigHandler() {
        return CONFIG_HANDLER;
    }

    public String getServerInterface() {
        return serverInterface;
    }
}
