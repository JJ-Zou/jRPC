package com.zjj.registry.support;

import com.zjj.common.JRpcURL;
import com.zjj.registry.NotifyListener;
import com.zjj.registry.ServiceListener;

import java.util.List;
import java.util.Set;

public class ServiceFailbackRegistry extends FailbackRegistry {
    protected ServiceFailbackRegistry(JRpcURL url) {
        super(url);
    }

    @Override
    protected void doRegister(JRpcURL url) {

    }

    @Override
    protected void doUnregister(JRpcURL url) {

    }

    @Override
    protected void doSubscribe(JRpcURL url, NotifyListener listener) {

    }

    @Override
    protected void doUnsubscribe(JRpcURL url, NotifyListener listener) {

    }

    @Override
    protected void doAvailable(JRpcURL url) {

    }

    @Override
    protected void doUnavailable(JRpcURL url) {

    }

    @Override
    protected List<JRpcURL> doDiscover(JRpcURL url) {
        return null;
    }

}
