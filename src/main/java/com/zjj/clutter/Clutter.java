package com.zjj.clutter;

import com.zjj.common.JRpcURL;
import com.zjj.rpc.Caller;
import com.zjj.rpc.Reference;

import java.util.List;

public interface Clutter<T> extends Caller<T> {

    void setUrl(JRpcURL url);

    void setLoadBalance(LoadBalance<T> loadBalance);

    void setHaStrategy(HaStrategy<T> haStrategy);

    void onRefresh(List<Reference<T>> references);

    LoadBalance<T> getLoadBalance();

    List<Reference<T>> getReferences();

}
