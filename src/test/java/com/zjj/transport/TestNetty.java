package com.zjj.transport;

import com.zjj.common.JRpcURL;
import com.zjj.common.JRpcURLParamType;
import com.zjj.extension.ExtensionLoader;
import com.zjj.transport.netty.server.NettyEndpointFactory;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class TestNetty {
    @Test
    public void nettyFactory() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("address", "39.105.65.104:2181");
        parameters.put(JRpcURLParamType.registryRetryPeriod.getName(), "1000");
        JRpcURL jRpcURL = new JRpcURL("jrpc", "127.0.0.1", 20855, "com.zjj.registry.zookeeper", parameters);
        Server server = new NettyEndpointFactory().createServer(jRpcURL, null);
        server.open();
    }

    @Test
    public void heartbeatFactory() {
        System.out.println(HeartBeatFactory.class);
        HeartBeatFactory defaultExtension = ExtensionLoader.getExtensionLoader(HeartBeatFactory.class).getDefaultExtension();
        System.out.println(defaultExtension.getClass());
    }

    @Test
    public void endPointFactory() {
        System.out.println(EndpointFactory.class);
        EndpointFactory defaultExtension = ExtensionLoader.getExtensionLoader(EndpointFactory.class).getDefaultExtension();
        System.out.println(defaultExtension.getClass());
    }
}
