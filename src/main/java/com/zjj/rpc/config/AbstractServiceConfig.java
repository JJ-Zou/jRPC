package com.zjj.rpc.config;

public abstract class AbstractServiceConfig extends AbstractConfig {
    private static final long serialVersionUID = 6192650812518633020L;
    protected Boolean export;

    public Boolean getExport() {
        return export;
    }

    public void setExport(Boolean export) {
        this.export = export;
    }


}
