package com.zjj.clutter.ha;

import com.zjj.clutter.LoadBalance;
import com.zjj.common.JRpcURL;
import com.zjj.common.JRpcURLParamType;
import com.zjj.rpc.Reference;
import com.zjj.rpc.Request;
import com.zjj.rpc.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class FailoverHaStrategy<T> extends AbstractHaStrategy<T> {
    private final ThreadLocal<List<Reference<T>>> referThreadLocal = ThreadLocal.withInitial(ArrayList::new);

    @Override
    public Response call(Request request, LoadBalance<T> loadBalance) {
        List<Reference<T>> references = referThreadLocal.get();
        loadBalance.selectToHolder(request, references);
        if (references.isEmpty()) {
            throw new IllegalStateException("FailoverHaStrategy has no reference for request " + references);
        }
        JRpcURL refUrl = references.get(0).getUrl();
        int retries = refUrl.getMethodParameter(request.getMethodName(), request.getParameterSign(),
                JRpcURLParamType.RETRIES.getName(), JRpcURLParamType.RETRIES.getIntValue());
        if (retries < 0) {
            retries = 0;
        }
        for (int i = 0; i <= retries; i++) {
            Reference<T> reference = references.get(i % references.size());
            try {
                request.setRetries(i);
                return reference.call(request);
            } catch (Exception e) {
                if (i >= retries) {
                    throw e;
                }
                log.warn("FailoverHaStrategy call false for request {}", request, e);
            }
        }
        throw new IllegalStateException();
    }

    public void destroy() {
        referThreadLocal.remove();
    }


}
