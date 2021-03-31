package com.zjj.jrpc.config;

import com.zjj.jrpc.common.JRpcURL;
import com.zjj.jrpc.common.JRpcURLParamType;
import com.zjj.jrpc.common.utils.NetUtils;
import com.zjj.jrpc.config.annotation.Ignore;
import com.zjj.jrpc.extension.ExtensionLoader;
import com.zjj.jrpc.rpc.Exporter;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Setter
@Getter
public class ServiceConfig<T> extends AbstractInterfaceConfig {
    private static final long serialVersionUID = 7509808393399061064L;

    private static final ConfigHandler CONFIG_HANDLER = ExtensionLoader.getExtensionLoader(ConfigHandler.class).getDefaultExtension();
    private static final Set<String> ACTIVE_SERVICES = ConcurrentHashMap.newKeySet();
    private final Set<Exporter<T>> exporters = ConcurrentHashMap.newKeySet();
    protected String exportHost;
    protected boolean export;
    @Ignore
    protected boolean isDefault;
    private transient Class<T> interfaceClass;
    private transient T ref;
    private String refBeanName;
    private String refInterfaceName;
    @Ignore
    private transient volatile boolean exported;
    @Ignore
    private transient volatile boolean unexported;


    protected void checkProtocol() {
        if (getProtocolConfigs().isEmpty()) {
            throw new IllegalStateException("Service protocolConfig must be set.");
        }
    }

    public void export() {
        if (exported) {
            log.warn("{} has already been exported, ignore this export request!", interfaceClass.getName());
            return;
        }
        checkProtocol();
        checkRegistry();
        checkInterfaceAndMethods(interfaceClass, methodConfigs);
        protocolConfigs.forEach(this::doExport);
        afterExport();
    }

    private void doExport(ProtocolConfig protocolConfig) {
        String protocolName = protocolConfig.getProtocolName();
        int exportPort = protocolConfig.getPort();
        if (StringUtils.isEmpty(protocolName)) {
            protocolName = JRpcURLParamType.PROTOCOL.getValue();
        }
        String exportAddr = exportHost;
        String localHostString = NetUtils.getLocalHostString();
        if (StringUtils.isEmpty(exportAddr)) {
            exportAddr = localHostString;
        }
        Map<String, String> params = new HashMap<>();
        params.put(JRpcURLParamType.NODE_TYPE.getName(), JRpcURLParamType.NODE_TYPE.getValue());
        params.put(JRpcURLParamType.REFRESH_TIMESTAMP.getName(), String.valueOf(System.currentTimeMillis()));
        collectConfigs(params, protocolConfig, this);
        collectMethodConfigs(params, methodConfigs);
        // exportAddr is the real IP
        JRpcURL exportServiceUrl = new JRpcURL(protocolName, exportAddr, exportPort, interfaceClass.getName(), params);
        // bindAddress is netty server bind address
        exportServiceUrl.setBindAddress(localHostString + ":" + exportPort);
        if (existService(exportServiceUrl)) {
            log.warn("{} already exist.", exportServiceUrl);
            return;
        }
        Exporter<T> exporter = CONFIG_HANDLER.export(interfaceClass, ref, registryUrls, exportServiceUrl);
        exporters.add(exporter);
    }

    private void afterExport() {
        exported = true;
        exporters.forEach(exporter -> ACTIVE_SERVICES.add(exporter.getProvider().getUrl().getIdentity()));
    }

    public boolean existService(JRpcURL url) {
        return ACTIVE_SERVICES.contains(url.getIdentity());
    }

    public void unExport() {
        if (unexported) {
            return;
        }
        try {
            CONFIG_HANDLER.unExport(exporters, registryUrls);
        } finally {
            afterUnExport();
        }
    }

    private void afterUnExport() {
        exported = false;
        unexported = true;
        exporters.forEach(exporter -> ACTIVE_SERVICES.remove(exporter.getProvider().getUrl().getIdentity()));
        exporters.clear();
    }

    public boolean isExported() {
        return exported;
    }

    public boolean isUnexported() {
        return unexported;
    }
}
