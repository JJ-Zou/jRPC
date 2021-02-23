package com.zjj.config.bootstrap;

import com.zjj.common.event.GenericEventListener;
import com.zjj.registry.RegistryFactory;
import com.zjj.registry.RegistryService;
import com.zjj.registry.zookeeper.ZookeeperRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class RpcBootstrap extends GenericEventListener {
    private static final String NAME = RpcBootstrap.class.getSimpleName();

    // 单例
    private static volatile RpcBootstrap instance;
    private final AtomicBoolean awaited = new AtomicBoolean(false);
    private final Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
    private final Lock destroyLock = new ReentrantLock();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private AtomicBoolean initialized = new AtomicBoolean(false);
    private AtomicBoolean started = new AtomicBoolean(false);
    private AtomicBoolean ready = new AtomicBoolean(true);
    private AtomicBoolean destroyed = new AtomicBoolean(false);

    public static RpcBootstrap getInstance() {
        if (instance == null) {
            synchronized (RpcBootstrap.class) {
                if (instance == null) {
                    instance = new RpcBootstrap();
                }
            }
        }
        return instance;
    }

    private RpcBootstrap() {
        super();

    }

    public RpcBootstrap start() {
        if (started.compareAndSet(false, true)) {
            ready.set(false);
            initialize();
            log.debug("{} is starting...", NAME);
            exportServices();


            log.debug("{} has started.", NAME);
        }
        return this;
    }


    public void initialize() {
        if (!initialized.compareAndSet(false, true)) {
            return;
        }

        log.debug("{} has been initialized.", NAME);
    }

    private void exportServices() {

    }

    public RpcBootstrap stop() throws IllegalStateException {

        return this;
    }
}
