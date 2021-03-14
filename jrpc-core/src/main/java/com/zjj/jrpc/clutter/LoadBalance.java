package com.zjj.jrpc.clutter;

import com.zjj.jrpc.rpc.Reference;
import com.zjj.jrpc.rpc.Request;

import java.util.List;

public interface LoadBalance<T> {
    void onRefresh(List<Reference<T>> references);

    Reference<T> select(Request request);

    void selectToHolder(Request request, List<Reference<T>> references);

    void setWeightString(String weightString);
}
