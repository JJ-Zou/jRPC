package com.zjj.jrpc.clutter;

import com.zjj.jrpc.common.JRpcURL;
import com.zjj.jrpc.rpc.Caller;
import com.zjj.jrpc.rpc.Reference;

import java.util.List;

public interface Clutter<T> extends Caller<T> {

    void setUrl(JRpcURL url);

    void setHaStrategy(HaStrategy<T> haStrategy);

    void onRefresh(List<Reference<T>> references);

    LoadBalance<T> getLoadBalance();

    void setLoadBalance(LoadBalance<T> loadBalance);

    List<Reference<T>> getReferences();

}
