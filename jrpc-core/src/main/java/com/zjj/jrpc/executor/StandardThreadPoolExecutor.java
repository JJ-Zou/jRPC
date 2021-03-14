package com.zjj.jrpc.executor;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class StandardThreadPoolExecutor extends ThreadPoolExecutor {

    public static final int DEFAULT_MAX_IDLE_TIME = 60 * 1000; // 1 minutes

    private final AtomicInteger runningTasks = new AtomicInteger(0);

    private final int maxConcurrencySize;

    public StandardThreadPoolExecutor(int corePoolSize,
                                      int maximumPoolSize,
                                      int workerQueueSize,
                                      ThreadFactory threadFactory) {
        this(corePoolSize, maximumPoolSize,
                DEFAULT_MAX_IDLE_TIME, TimeUnit.SECONDS,
                workerQueueSize,
                threadFactory,
                new AbortPolicy());
    }

    public StandardThreadPoolExecutor(int corePoolSize,
                                      int maximumPoolSize,
                                      long keepAliveTime,
                                      TimeUnit unit,
                                      int workerQueueSize,
                                      ThreadFactory threadFactory,
                                      RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, new TaskQueue(), threadFactory, handler);
        ((TaskQueue) getQueue()).setParent(this);
        prestartAllCoreThreads();
        maxConcurrencySize = getMaximumPoolSize() + workerQueueSize;
        log.info("Create a thread pool [{}] and prepare start {} core threads, available max concurrency is {}", this, corePoolSize, maxConcurrencySize);
    }

    @Override
    public void execute(Runnable command) {
        int running = runningTasks.get();
        if (running > maxConcurrencySize) {
            getRejectedExecutionHandler().rejectedExecution(command, this);
            runningTasks.decrementAndGet();
            return;
        }
        try {
            super.execute(command);
        } catch (RejectedExecutionException e) {
            if (!((TaskQueue) getQueue()).force(command)) {
                getRejectedExecutionHandler().rejectedExecution(command, this);
                runningTasks.decrementAndGet();
            }
        }
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        runningTasks.incrementAndGet();
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        runningTasks.decrementAndGet();
    }

    public int getRunningTasks() {
        return runningTasks.get();
    }

    static class TaskQueue extends LinkedTransferQueue<Runnable> {

        private static final long serialVersionUID = 2418641364420497764L;

        private transient StandardThreadPoolExecutor parent;

        public void setParent(StandardThreadPoolExecutor parent) {
            this.parent = parent;
        }

        public boolean force(Runnable r) {
            if (parent == null || parent.isShutdown()) {
                throw new RejectedExecutionException("task queue not running.");
            }
            // 强制交给父类queue，如果被拒绝则交给该queue
            return super.offer(r);
        }

        @Override
        public boolean offer(Runnable runnable) {
            if (parent.getPoolSize() == parent.getMaximumPoolSize()) {
                return super.offer(runnable);
            }
            if (parent.getRunningTasks() <= parent.getPoolSize()) {
                return super.offer(runnable);
            }
            if (parent.getPoolSize() < parent.getMaximumPoolSize()) {
                return false;
            }
            return super.offer(runnable);
        }
    }
}
