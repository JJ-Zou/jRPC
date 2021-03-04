package com.zjj.rpc.config;

import com.zjj.rpc.config.bootstrap.JRpcBootstrap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServiceConfig<T> extends ServiceConfigBase<T> {

    private static final long serialVersionUID = -6384966552477552552L;

    private JRpcBootstrap bootstrap;

    private transient volatile boolean exported;
    private transient volatile boolean unexported;

    @Override
    public void export() {
        if (!shouldExport()) {
            return;
        }
        if (bootstrap == null) {
            bootstrap = JRpcBootstrap.getInstance();
            bootstrap.initialize();
        }
        doExport();
        exported();
    }

    protected void doExport() {
        if (unexported) {
            throw new IllegalStateException("The service " + interfaceClass.getName() + " has already unexported!");
        }
        if (exported) {
            return;
        }
        exported = true;
        if (path == null || path.isEmpty()) {
            path = interfaceName;
        }
        doExportUrls();
    }

    private void doExportUrls() {
        log.info("doExportUrls");
    }

    public void exported() {

    }


    public JRpcBootstrap getBootstrap() {
        return bootstrap;
    }

    public void setBootstrap(JRpcBootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public String getBeanName() {
        return "";
    }

}
