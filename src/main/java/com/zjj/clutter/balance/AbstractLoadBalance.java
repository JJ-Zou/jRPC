package com.zjj.clutter.balance;

import com.zjj.clutter.LoadBalance;
import com.zjj.rpc.Reference;
import com.zjj.rpc.Request;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractLoadBalance<T> implements LoadBalance<T> {
    public static final int MAX_REFERENCE = 10;

    private List<Reference<T>> references;

    @Override
    public void onRefresh(List<Reference<T>> references) {
        Collections.shuffle(references);
        this.references = new ArrayList<>(references);
    }

    @Override
    public Reference<T> select(Request request) {
        return doSelect(request);
    }

    @Override
    public void selectToHolder(Request request, List<Reference<T>> references) {
        references.clear();
        doSelectToHolder(request, references);
    }

    @Override
    public void setWeightString(String weightString) {
        throw new IllegalStateException("Method setWeightString is unsupported");
    }

    public List<Reference<T>> getReferences() {
        return references;
    }

    protected abstract Reference<T> doSelect(Request request);

    protected abstract void doSelectToHolder(Request request, List<Reference<T>> references);
}
