package com.zjj.jrpc.clutter;

import com.zjj.jrpc.common.JRpcURL;
import com.zjj.jrpc.rpc.Request;
import com.zjj.jrpc.rpc.Response;

public interface HaStrategy<T> {
    void setUrl(JRpcURL url);

    Response call(Request request, LoadBalance<T> loadBalance);
}
