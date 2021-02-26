package com.zjj.rpc.config.bootstrap;

import com.zjj.rpc.common.JRpcURL;
import com.zjj.rpc.config.ConfigCenterConfig;
import com.zjj.rpc.config.RegistryConfig;
import com.zjj.rpc.config.ServiceConfig;
import com.zjj.rpc.config.ServiceConfigBase;
import com.zjj.rpc.config.configcenter.DynamicConfiguration;
import com.zjj.rpc.config.context.ConfigManager;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class JRpcBootstrap {

    private static volatile JRpcBootstrap instance;
    private static final String NAME = JRpcBootstrap.class.getSimpleName();

    private AtomicBoolean initialized = new AtomicBoolean(false);
    private AtomicBoolean started = new AtomicBoolean(false);
    private AtomicBoolean ready = new AtomicBoolean(true);
    private AtomicBoolean destroyed = new AtomicBoolean(false);


    private List<ServiceConfigBase<?>> exportedServices = new ArrayList<>();

    private JRpcBootstrap() {

    }

    public static JRpcBootstrap getInstance() {
        if (instance == null) {
            synchronized (JRpcBootstrap.class) {
                if (instance == null) {
                    instance = new JRpcBootstrap();
                }
            }
        }
        return instance;
    }

    public JRpcBootstrap start() {
        if (!started.compareAndSet(false, true)) {
            return this;
        }
        ready.set(false);
        initialize();
        log.info("{} is starting...", NAME);
        exportServices();
        ready.set(true);
        log.info("{} has started...", NAME);
        return this;
    }


    public void initialize() {
        if (!initialized.compareAndSet(false, true)) {
            return;
        }
        startConfigCenter();
    }

    private void startConfigCenter() {
        RegistryConfig registryConfig = (RegistryConfig) ConfigManager.getConfig(RegistryConfig.class, RegistryConfig.BEAN_NAME);
        if (registryConfig == null) {
            throw new IllegalArgumentException("instance of bean " + RegistryConfig.class + " has not been creating");
        }
        registryAsConfigCenterRegistry(registryConfig);
        ConfigCenterConfig configCenterConfig = (ConfigCenterConfig) ConfigManager.getConfig(ConfigCenterConfig.class, ConfigCenterConfig.BEAN_NAME);
        prepareEnvironment(configCenterConfig);
    }

    private void prepareEnvironment(ConfigCenterConfig configCenterConfig) {
        if (!configCenterConfig.checkOrUpdateInited()) {
            return;
        }
        JRpcURL jRpcURL = configCenterConfig.toUrl();
        DynamicConfiguration dynamicConfiguration = DynamicConfiguration.getDynamicConfiguration(jRpcURL);

    }

    private void registryAsConfigCenterRegistry(RegistryConfig registryConfig) {
        String protocol = registryConfig.getProtocol();
        Integer port = registryConfig.getPort();
        String id = "config-center-" + protocol + "-" + port;
        ConfigCenterConfig configCenterConfig = new ConfigCenterConfig();
        configCenterConfig.setId(id);
        configCenterConfig.setProtocol(protocol);
        configCenterConfig.setAddress(registryConfig.getProtocol() + "://" + registryConfig.getAddress());
        configCenterConfig.setPort(port);
        log.info("The registry {} will be used as the config center.", registryConfig);
        ConfigManager.addConfig(configCenterConfig);
    }


    private void exportServices() {
        ConfigManager.getServices().forEach(serviceConfigBase -> {
            ((ServiceConfig) serviceConfigBase).setBootstrap(this);
            serviceConfigBase.export();
            exportedServices.add(serviceConfigBase);
        });
    }

    public JRpcBootstrap stop() {
        return this;
    }
}
