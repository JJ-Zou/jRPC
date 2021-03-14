package com.zjj.jrpc.rpc.support;

import com.zjj.jrpc.common.JRpcURL;
import com.zjj.jrpc.exception.JRpcErrorMessage;
import com.zjj.jrpc.exception.JRpcServiceProviderException;
import com.zjj.jrpc.rpc.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DefaultResponseFuture<V> extends ResponseFuture<V> {

    private final Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
    private final JRpcURL serverUrl;
    private final long createTime = System.currentTimeMillis();
    private final int timeout;
    private final Request request;
    private final CopyOnWriteArrayList<FutureListener<? extends JFuture<? super V>>> listeners = new CopyOnWriteArrayList<>();
    private Exception exception;
    private long processTime;
    private Class<V> returnType;
    private Map<String, String> attachments;
    private Object result;

    public DefaultResponseFuture(JRpcURL serverUrl, int timeout, Request request) {
        this.serverUrl = serverUrl;
        this.timeout = timeout;
        this.request = request;
    }

    @Override
    public void onSuccess(Response response) {
        this.result = response.getValue();
        this.processTime = response.getProcessTime();
        this.attachments = response.getAttachments();
        done();
    }

    @Override
    public void onFailure(Response response) {
        if (exception != null &&
                ((exception instanceof JRpcServiceProviderException) && ((JRpcServiceProviderException) exception).getErrorMessage() == JRpcErrorMessage.SERVICE_TIMEOUT_ERROR)) {
            return;
        }
        this.exception = response.getException();
        this.processTime = response.getProcessTime();
        done();
    }

    private boolean done() {
        if (RESULT_UPDATE.compareAndSet(this, RUNNING, DONE)) {
            notifyListeners();
            try {
                lock.lock();
                condition.signalAll();
            } finally {
                lock.unlock();
            }
            return true;
        }
        return false;
    }


    private void notifyListeners() {
        listeners.forEach(this::notifyListener);
    }


    private void notifyListener(FutureListener listener) {
        listener.operationComplete(this);
    }

    private boolean isRunning() {
        return this.state == RUNNING;
    }

    @Override
    public Object getValue() {
        try {
            return get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.interrupted();
            e.printStackTrace();
            return null;
        }
    }

    private Object getValueOrThrowable() {
        if (exception != null) {
            if (exception instanceof RuntimeException) {
                throw (RuntimeException) exception;
            }
            throw new JRpcServiceProviderException(exception.getMessage(), exception);
        }
        return result;
    }

    @Override
    public void addListener(FutureListener<? extends JFuture<? super V>> listener) {
        if (!isRunning()) {
            notifyListener(listener);
            return;
        }
        listeners.add(listener);
    }


    @SuppressWarnings("unchecked")
    @Override
    public V get() throws InterruptedException, ExecutionException {
        try {
            lock.lock();
            if (!isRunning()) {
                return (V) getValueOrThrowable();
            }
            if (timeout <= 0) {
                // never
                condition.await();
                return (V) getValueOrThrowable();
            } else {
                long waitTime = timeout - (System.currentTimeMillis() - createTime);
                for (; ; ) {
                    if (waitTime <= 0 || !isRunning()) {
                        break;
                    } else {
                        condition.await(waitTime, TimeUnit.MILLISECONDS);
                        waitTime = timeout - (System.currentTimeMillis() - createTime);
                    }
                }
                timeoutCancel();
            }
            return (V) getValueOrThrowable();
        } finally {
            lock.unlock();
        }
    }

    private boolean timeoutCancel() {
        this.processTime = System.currentTimeMillis() - createTime;
        if (RESULT_UPDATE.compareAndSet(this, RUNNING, CANCELED)) {
            this.exception = new JRpcServiceProviderException(this.getClass().getSimpleName() + " cancel this task: " + request + ", cost " + (System.currentTimeMillis() - createTime) + "ms", JRpcErrorMessage.SERVICE_TIMEOUT_ERROR);
            try {
                lock.lock();
                condition.signalAll();
            } finally {
                lock.unlock();
                notifyListeners();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean cancel() {
        if (RESULT_UPDATE.compareAndSet(this, RUNNING, CANCELED)) {
            this.processTime = System.currentTimeMillis() - createTime;
            this.exception = new JRpcServiceProviderException(this.getClass().getSimpleName() + " cancel this task: " + request + ", cost " + (System.currentTimeMillis() - createTime) + "ms");
            try {
                lock.lock();
                condition.signalAll();
            } finally {
                lock.unlock();
                notifyListeners();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean isCancelled() {
        return this.state == CANCELED;
    }

    @Override
    public boolean isSuccess() {
        return isDone() && exception == null;
    }

    @Override
    public boolean isDone() {
        return state != null && state != CANCELED;
    }

    @Override
    public void setReturnType(Class<V> clazz) {
        this.returnType = clazz;
    }

    @Override
    public long getCreateTime() {
        return createTime;
    }

    @Override
    public Exception getException() {
        return exception;
    }

    @Override
    public long getRequestId() {
        return request.getRequestId();
    }

    @Override
    public long getProcessTime() {
        return processTime;
    }

    @Override
    public void setProcessTime(long processTime) {
        this.processTime = processTime;
    }

    @Override
    public int getTimeout() {
        return timeout;
    }

    @Override
    public Map<String, String> getAttachments() {
        return attachments == null ? Collections.emptyMap() : Collections.unmodifiableMap(attachments);
    }

    public void setAttachments(Map<String, String> attachments) {
        this.attachments = new HashMap<>(attachments);
    }

    @Override
    public void setAttachment(String key, String value) {
        if (attachments == null) {
            attachments = new HashMap<>();
        }
        attachments.put(key, value);
    }

    @Override
    public boolean containsAttachment(String key) {
        if (attachments == null) {
            return false;
        }
        return attachments.containsKey(key);
    }

    @Override
    public String getAttachment(String key) {
        return attachments == null ? null : attachments.get(key);
    }


}
