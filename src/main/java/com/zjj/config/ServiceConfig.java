package com.zjj.config;

import com.zjj.common.JRpcURL;
import com.zjj.common.JRpcURLParamType;
import com.zjj.common.utils.NetUtils;
import com.zjj.extension.ExtensionLoader;
import com.zjj.rpc.Exporter;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Setter
@Getter
public class ServiceConfig<T> extends AbstractServiceConfig {
    private static final long serialVersionUID = 7509808393399061064L;
    private static final ConfigHandler CONFIG_HANDLER = ExtensionLoader.getExtensionLoader(ConfigHandler.class).getDefaultExtension();

    private static final Set<String> ACTIVE_SERVICES = ConcurrentHashMap.newKeySet();

    private final Set<Exporter<T>> exporters = ConcurrentHashMap.newKeySet();

    protected transient List<MethodConfig> methodConfigs;

    private transient T ref;

    private transient Class<T> interfaceClass;

    private transient volatile boolean exported;
    private transient volatile boolean unexported;

    @Override
    public void export() {
        if (exported) {
            log.warn("{} has already been exported, ignore this export request!", interfaceClass.getName());
            return;
        }
        Map<String, Integer> protocolPorts = checkAndGetProtocol();
        checkRegistry();
        checkInterfaceAndMethods(interfaceClass, methodConfigs);
        protocolConfigs.forEach(protocolConfig -> {
            int exportPort = protocolPorts.get(protocolConfig.getId());
            doExport(protocolConfig, exportPort);
        });
        afterExport();
    }

    private void doExport(ProtocolConfig protocolConfig, int exportPort) {
        String protocolName = protocolConfig.getName();
        if (StringUtils.isEmpty(protocolName)) {
            protocolName = JRpcURLParamType.protocol.getValue();
        }
        String exportAddr = exportHost;
        if (StringUtils.isEmpty(exportAddr)) {
            exportAddr = NetUtils.getLocalHostString();
        }
        Map<String, String> params = new HashMap<>();
        params.put(JRpcURLParamType.nodeType.getName(), JRpcURLParamType.nodeType.getValue());
        params.put(JRpcURLParamType.refreshTimestamp.getName(), String.valueOf(System.currentTimeMillis()));
        refreshConfigs(params, protocolConfig, this);
        refreshMethodConfigs(params, methodConfigs);
        JRpcURL refUrl = new JRpcURL(protocolName, exportAddr, exportPort, interfaceClass.getName(), params);
        if (existService(refUrl)) {
            log.warn("{} already exist.", refUrl);
            return;
        }
        Exporter<T> exporter = CONFIG_HANDLER.export(interfaceClass, ref, registryUrls, refUrl);
        exporters.add(exporter);
    }

    private void afterExport() {
        exported = true;
        exporters.forEach(exporter -> ACTIVE_SERVICES.add(exporter.getProvider().getUrl().getIdentity()));
    }

    public boolean existService(JRpcURL url) {
        return ACTIVE_SERVICES.contains(url.getIdentity());
    }

    @Override
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

    @Override
    public boolean isExported() {
        return exported;
    }

    @Override
    public boolean isUnexported() {
        return unexported;
    }
}
