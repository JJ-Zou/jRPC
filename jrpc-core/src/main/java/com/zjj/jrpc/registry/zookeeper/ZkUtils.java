package com.zjj.jrpc.registry.zookeeper;

import com.zjj.jrpc.common.JRpcURL;

public class ZkUtils {
    private ZkUtils() {

    }

    public static String toNodePath(JRpcURL url, ZkNodeType nodeType) {
        return toNodeTypePath(url, nodeType) + "/" + url.getBindAddress();
    }

    public static String toNodeTypePath(JRpcURL url, ZkNodeType nodeType) {
        return toServicePath(url) + "/" + nodeType.getValue();
    }

    public static String toServicePath(JRpcURL url) {
        return toGroupPath(url) + "/" + url.getPath();
    }

    public static String toGroupPath(JRpcURL url) {
        return "/" + url.getProtocol() + "/" + url.getGroup();
    }
}
