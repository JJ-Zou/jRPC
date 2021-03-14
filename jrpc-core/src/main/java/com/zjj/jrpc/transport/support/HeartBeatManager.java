package com.zjj.jrpc.transport.support;

import com.zjj.jrpc.common.JRpcURLParamType;
import com.zjj.jrpc.exception.JRpcFrameworkException;
import com.zjj.jrpc.extension.ExtensionLoader;
import com.zjj.jrpc.transport.Client;
import com.zjj.jrpc.transport.Endpoint;
import com.zjj.jrpc.transport.EndpointManager;
import com.zjj.jrpc.transport.HeartBeatFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

@Slf4j
public class HeartBeatManager implements EndpointManager {
    private final ConcurrentMap<Client, HeartBeatFactory> HEART_BEAT_FACTORIES = new ConcurrentHashMap<>();

    private final ScheduledExecutorService executor;

    public HeartBeatManager() {
        executor = Executors.newSingleThreadScheduledExecutor();
        init();
    }

    @Override
    public void init() {
        executor.scheduleAtFixedRate(() -> HEART_BEAT_FACTORIES.entrySet().stream()
                        .filter(entry -> !entry.getKey().isAvailable())
                        .forEach(entry -> {
                            try {
                                entry.getKey().heartbeat(entry.getValue().createRequest());
                            } catch (Exception e) {
                                log.error("HeartBeatManager send heartbeat to {} error.", entry.getKey().getUrl().getUri(), e);
                            }
                        }),
                JRpcURLParamType.HEART_BEAT_PERIOD.getIntValue(),
                JRpcURLParamType.HEART_BEAT_PERIOD.getIntValue(),
                TimeUnit.MILLISECONDS);
    }


    @Override
    public void destroy() {
        executor.shutdown();
    }

    @Override
    public void addEndPoint(Endpoint endPoint) {
        if (!(endPoint instanceof Client)) {
            throw new JRpcFrameworkException("HeartBeatManager addEndPoint error, unsupported type " + endPoint.getClass());
        }
        HeartBeatFactory heartBeatFactory = ExtensionLoader.getExtensionLoader(HeartBeatFactory.class).getDefaultExtension();
        HEART_BEAT_FACTORIES.put((Client) endPoint, heartBeatFactory);
    }

    @Override
    public void removeEndPoint(Endpoint endPoint) {
        if (!(endPoint instanceof Client)) {
            throw new JRpcFrameworkException("HeartBeatManager removeEndPoint error, unsupported type " + endPoint.getClass());
        }
        HEART_BEAT_FACTORIES.remove(endPoint);
    }
}
