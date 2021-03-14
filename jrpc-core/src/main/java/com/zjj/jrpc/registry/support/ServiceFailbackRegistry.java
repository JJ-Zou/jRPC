package com.zjj.jrpc.registry.support;

import com.zjj.jrpc.common.JRpcURL;
import com.zjj.jrpc.registry.NotifyListener;
import com.zjj.jrpc.registry.ServiceListener;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public abstract class ServiceFailbackRegistry extends FailbackRegistry {

    private final ConcurrentMap<JRpcURL, ServiceManager> serviceManagerMap = new ConcurrentHashMap<>();

    protected ServiceFailbackRegistry(JRpcURL url) {
        super(url);
    }

    @Override
    protected void doSubscribe(JRpcURL url, NotifyListener listener) {
        log.info("ServiceFailbackRegistry subscribe URL ({}).", url);
        ServiceManager serviceManager = getServiceManager(url);
        serviceManager.addNotifyListener(listener);
        subscribeService(url, serviceManager);
        notify(url, listener, doDiscover(url));
    }


    @Override
    protected void doUnsubscribe(JRpcURL url, NotifyListener listener) {
        log.info("ServiceFailbackRegistry unsubscribe URL ({}).", url);
        ServiceManager serviceManager = getServiceManager(url);
        serviceManager.removeNotifyListener(listener);
        unsubscribeService(url, serviceManager);
    }

    @Override
    protected List<JRpcURL> doDiscover(JRpcURL url) {
        log.info("ServiceFailbackRegistry discover URL ({}).", url);
        List<JRpcURL> urls = discoverService(url);
        log.info("ServiceFailbackRegistry discover result of  URL ({}) is {} size = {}.", url, urls, urls.size());
        return urls;
    }


    private ServiceManager getServiceManager(JRpcURL url) {
        ServiceManager serviceManager = serviceManagerMap.get(url);
        if (serviceManager == null) {
            synchronized (ServiceFailbackRegistry.class) {
                serviceManager = new ServiceManager(url, this);
                serviceManagerMap.put(url, serviceManager);
            }
        }
        return serviceManager;
    }

    protected abstract void subscribeService(JRpcURL url, ServiceListener listener);

    protected abstract void unsubscribeService(JRpcURL url, ServiceListener listener);

    protected abstract List<JRpcURL> discoverService(JRpcURL url);

}
