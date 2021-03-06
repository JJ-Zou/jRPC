package com.zjj.clutter.balance;

import com.zjj.rpc.Reference;
import com.zjj.rpc.Request;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class RoundRobinLoadBalance<T> extends AbstractLoadBalance<T> {
    private final AtomicInteger idx = new AtomicInteger(0);

    @Override
    protected Reference<T> doSelect(Request request) {
        int num = idx.incrementAndGet();
        num &= 0x0ffffff;
        List<Reference<T>> references = getReferences();
        int size = references.size();
        for (int i = 0; i < size; i++) {
            Reference<T> ref = references.get((i + num) % size);
            if (ref.isAvailable()) {
                return ref;
            }
        }
        return null;
    }


    @Override
    protected void doSelectToHolder(Request request, List<Reference<T>> references) {
        List<Reference<T>> refs = getReferences();
        references.addAll(refs.stream()
                .filter(Reference::isAvailable)
                .limit(MAX_REFERENCE)
                .collect(Collectors.toList()));
    }
}
