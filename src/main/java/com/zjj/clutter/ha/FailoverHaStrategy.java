package com.zjj.clutter.ha;

import com.zjj.clutter.LoadBalance;
import com.zjj.rpc.Reference;
import com.zjj.rpc.Request;
import com.zjj.rpc.Response;

import java.util.ArrayList;
import java.util.List;

public class FailoverHaStrategy<T> extends AbstractHaStrategy<T> {
    private final ThreadLocal<List<Reference<T>>> referThreadLocal = ThreadLocal.withInitial(ArrayList::new);


    @Override
    public Response call(Request request, LoadBalance<T> loadBalance) {
        List<Reference<T>> references = referThreadLocal.get();
        loadBalance.selectToHolder(request, references);
        //todo
        return null;
    }


}
