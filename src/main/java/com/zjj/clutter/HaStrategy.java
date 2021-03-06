package com.zjj.clutter;

import com.zjj.common.JRpcURL;
import com.zjj.rpc.Request;
import com.zjj.rpc.Response;

public interface HaStrategy<T> {
    void setUrl(JRpcURL url);

    Response call(Request request, LoadBalance<T> loadBalance);
}
