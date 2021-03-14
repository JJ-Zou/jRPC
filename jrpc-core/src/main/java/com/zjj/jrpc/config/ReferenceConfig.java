package com.zjj.jrpc.config;

import com.zjj.jrpc.clutter.Clutter;
import com.zjj.jrpc.clutter.clutter.ClutterNotify;
import com.zjj.jrpc.common.JRpcURL;
import com.zjj.jrpc.common.JRpcURLParamType;
import com.zjj.jrpc.common.utils.NetUtils;
import com.zjj.jrpc.config.annotation.Ignore;
import com.zjj.jrpc.extension.ExtensionLoader;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Setter
@Getter
@Slf4j
public class ReferenceConfig<T> extends AbstractInterfaceConfig {
    private static final long serialVersionUID = -8529195602996641811L;

    private static final ConfigHandler CONFIG_HANDLER = ExtensionLoader.getExtensionLoader(ConfigHandler.class).getDefaultExtension();

    private final List<ClutterNotify<T>> clutterNotifies = new ArrayList<>();

    @Ignore
    private transient volatile boolean initialized;
    @Ignore
    private transient volatile boolean destroyed;

    private Class<T> interfaceClass;

    private String directAddress;

    private transient T ref;

    public T getRef() {
        if (ref == null) {
            init();
        }
        log.info("get ref instance: {} type: {}", ref, ref.getClass());
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
            params.put(JRpcURLParamType.NODE_TYPE.getName(), JRpcURLParamType.REFERER.getValue());
            params.put(JRpcURLParamType.VERSION.getName(), JRpcURLParamType.VERSION.getValue());
            params.put(JRpcURLParamType.REFRESH_TIMESTAMP.getName(), String.valueOf(System.currentTimeMillis()));
            //
            collectConfigs(params, protocolConfig, this);
            collectMethodConfigs(params, methodConfigs);
            //
            String path = interfaceClass.getName();
            JRpcURL refUrl = new JRpcURL(protocolConfig.getProtocolName(), localIp, path, params);
            ClutterNotify<T> clutterNotify = createClutterNotify(refUrl);
            clutterNotifies.add(clutterNotify);
            return clutterNotify.getClutter();
        }).collect(Collectors.toList());
        ref = CONFIG_HANDLER.refer(interfaceClass, clutters);
        initialized = true;
    }

    private ClutterNotify<T> createClutterNotify(JRpcURL refUrl) {
        if (!StringUtils.isEmpty(directAddress)) {

        }
        return CONFIG_HANDLER.getClutterNotify(interfaceClass, registryUrls, refUrl);
    }

}
