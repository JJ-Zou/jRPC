package com.zjj.rpc.common;

import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JRpcURL implements Serializable {
    private static final long serialVersionUID = 2667503934364588762L;
    private final String protocol;
    private final String host;
    private final int port;
    private final String path;

    private final Map<String, String> parameters;


    private transient String address;

    private volatile transient Map<String, Number> numbers;

    public JRpcURL(String protocol, String host, int port) {
        this(protocol, host, port, null);
    }

    public JRpcURL(String protocol,
                   String host,
                   int port,
                   String path) {
        this(protocol, host, port, path, null);
    }

    public JRpcURL(String protocol,
                   String host,
                   int port,
                   String path,
                   Map<String, String> parameters) {
        int i = host.indexOf("://");
        if (i > 0) {
            this.protocol = host.substring(0, i);
            this.host = host.substring(i + 3);
        } else {
            this.protocol = protocol;
            this.host = host;
        }
        this.port = Math.max(port, 0);
        this.path = path;
        this.address = getAddress(this.host, this.port);
        this.parameters = parameters == null ? Collections.emptyMap() : Collections.unmodifiableMap(parameters);
    }

    public String getAddress() {
        return address;
    }

    private static String getAddress(String host, int port) {
        return port <= 0 ? host : host + ':' + port;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getPath() {
        return path;
    }

    public String getBackupAddress() {
        return getAddress();
    }

    public int getParameter(String key, int defaultValue) {
        Number n = getNumbers().get(key);
        if (n != null) {
            return n.intValue();
        }
        String value = getParameter(key);
        if (StringUtils.isEmpty(value)) {
            return defaultValue;
        }
        int result = Integer.parseInt(value);
        getNumbers().put(key, result);
        return result;
    }

    public String getParameter(String key) {
        return parameters.get(key);
    }

    private Map<String, Number> getNumbers() {
        if (numbers == null) {
            numbers = new ConcurrentHashMap<>();
        }
        return numbers;
    }


    @Override
    public String toString() {
        return protocol +
                "://" +
                host +
                ":" +
                port +
                "/" +
                path;
    }
}
