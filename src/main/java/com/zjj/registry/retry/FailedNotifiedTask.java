package com.zjj.registry.retry;

import com.zjj.common.URL;
import com.zjj.common.utils.CollectionUtils;
import com.zjj.registry.Constants;
import com.zjj.registry.NotifyListener;
import com.zjj.registry.support.FailbackRegistry;
import com.zjj.common.timer.Timeout;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class FailedNotifiedTask extends AbstractRetryTask {
    private final NotifyListener listener;

    private final List<URL> urls = new CopyOnWriteArrayList<>();

    public FailedNotifiedTask(URL url, NotifyListener listener) {
        super(url, null, Constants.RETRY_NOTIFY_NAME);
        if (listener == null) {
            throw new IllegalArgumentException();
        }
        this.listener = listener;
    }

    public void addUrlToRetry(List<URL> urls) {
        if (CollectionUtils.isEmpty(urls)) {
            return;
        }
        this.urls.addAll(urls);
    }

    public void removeRetryUrl(List<URL> urls) {
        this.urls.removeAll(urls);
    }

    @Override
    protected void doRetry(URL url, FailbackRegistry registry, Timeout timeout) {
        if (CollectionUtils.isNotEmpty(urls)) {
            listener.notify(urls);
            urls.clear();
        }
        reput(timeout, retryPeriod);
    }
}
