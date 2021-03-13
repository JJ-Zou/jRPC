package com.zjj.config;

import com.zjj.common.JRpcURL;
import com.zjj.common.JRpcURLParamType;
import com.zjj.common.utils.NetUtils;
import com.zjj.config.annotation.Ignore;
import com.zjj.extension.ExtensionLoader;
import com.zjj.rpc.Exporter;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Setter
@Getter
public class ServiceConfig<T> extends AbstractInterfaceConfig {
    private static final long serialVersionUID = 7509808393399061064L;

    private static final ConfigHandler CONFIG_HANDLER = ExtensionLoader.getExtensionLoader(ConfigHandler.class).getDefaultExtension();
    private static final Set<String> ACTIVE_SERVICES = ConcurrentHashMap.newKeySet();
    private final Set<Exporter<T>> exporters = ConcurrentHashMap.newKeySet();

    private transient Class<T> interfaceClass;
    private transient T ref;

    private String refBeanName;
    private String refInterfaceName;
    protected String exportProtocol;
    protected String exportHost;
    protected boolean export;

    @Ignore
    protected boolean isDefault;
    @Ignore
    private transient volatile boolean exported;
    @Ignore
    private transient volatile boolean unexported;


    /**
     * 将 exportProtocol 解析为map
     *
     * @return map -> (key = protocol_id, value = export_port)
     */
    protected Map<String, Integer> checkAndGetProtocol() {
        if (StringUtils.isEmpty(exportProtocol)) {
            throw new IllegalStateException("Service exportProtocol must be set.");
        }
        return Arrays.stream(JRpcURLParamType.COMMA_SPLIT_PATTERN.getPattern().split(exportProtocol))
                .map(protocol -> JRpcURLParamType.COLON_SPLIT_PATTERN.getPattern().split(protocol))
                .collect(Collectors.toMap(protocolArr -> protocolArr[0], protocolArr -> Integer.parseInt(protocolArr[1])));
    }

    public void export() {
        if (exported) {
            log.warn("{} has already been exported, ignore this export request!", interfaceClass.getName());
            return;
        }
        Map<String, Integer> protocolPorts = checkAndGetProtocol();
        checkRegistry();
        checkInterfaceAndMethods(interfaceClass, methodConfigs);
        protocolConfigs.stream()
                .filter(p -> protocolPorts.containsKey(p.getId()))
                .forEach(protocolConfig -> {
                    int exportPort = protocolPorts.get(protocolConfig.getId());
                    doExport(protocolConfig, exportPort);
                });
        afterExport();
    }

    private void doExport(ProtocolConfig protocolConfig, int exportPort) {
        String protocolName = protocolConfig.getProtocolName();
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
        JRpcURL refUrl = new JRpcURL(protocolName, exportAddr, exportPort, interfaceClass.getName(), params);
        refUrl.setExportAddress(localHostString + ":" + exportPort);
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
