package com.zjj.rpc.config;

public abstract class ServiceConfigBase<T> extends AbstractServiceConfig {

    private static final long serialVersionUID = 4599328817296065607L;
    protected String interfaceName;
    protected Class<?> interfaceClass;

    protected T ref;

    protected String path;

    protected ProviderConfig provider;

    public ServiceConfigBase() {
    }

    public boolean shouldExport() {
        Boolean export = getExport();
        return export == null || export;
    }

    public void setInterfaceClass(Class<?> interfaceClass) {
        setInterface(interfaceClass);
    }

    public void setInterface(Class<?> interfaceClass) {
        this.interfaceClass = interfaceClass;
        setInterface(interfaceClass == null ? null : interfaceClass.getName());
    }

    public Class<?> getInterfaceClass() {
        if (interfaceClass != null) {
            return interfaceClass;
        }
        try {
            if (interfaceName != null && !interfaceName.isEmpty()) {
                interfaceClass = Class.forName(interfaceName, true, Thread.currentThread().getContextClassLoader());
            }
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        return interfaceClass;
    }

    public void setInterface(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getInterface() {
        return interfaceName;
    }

    @Override
    public Boolean getExport() {
        return (export == null && provider != null) ? provider.getExport() : export;
    }

    public abstract void export();
}
