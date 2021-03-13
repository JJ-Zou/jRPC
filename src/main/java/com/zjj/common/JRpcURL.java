package com.zjj.common;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@Getter
public class JRpcURL {

    private final String protocol;
    private final String host;
    private final int port;
    // 接口名称
    private final String path;
    private final Map<String, String> parameters;

    private volatile transient Map<String, Number> cachedNumber;

    public JRpcURL(String protocol, String host, String path) {
        this(protocol, host, 0, path, Collections.emptyMap());
    }

    public JRpcURL(String protocol, String host, int port, String path) {
        this(protocol, host, port, path, Collections.emptyMap());
    }

    public JRpcURL(String protocol, String host, String path, Map<String, String> parameters) {
        this(protocol, host, 0, path, parameters);
    }

    public JRpcURL(String protocol, String host, int port, String path, Map<String, String> parameters) {
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.path = path;
        this.parameters = parameters;
    }

    public Map<String, String> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }

    public static JRpcURL valueOf(String url) {
        if (StringUtils.isBlank(url)) {
            throw new IllegalArgumentException("url == null!");
        }
        String protocol = null;
        String host = null;
        int port = 0;
        String path = null;
        Map<String, String> parameters = new HashMap<>();
        int i = url.indexOf("?");
        if (i > 0) {
            Stream.of(url.substring(i + 1).split("&"))
                    .map(String::trim)
                    .filter(str -> str.length() > 0)
                    .forEach(entry -> {
                        int index = entry.indexOf("=");
                        if (index >= 0) {
                            parameters.put(entry.substring(0, index), entry.substring(index + 1));
                        } else {
                            parameters.put(entry, entry);
                        }
                    });
            url = url.substring(0, i);
        }
        i = url.indexOf("://");
        if (i > 0) {
            protocol = url.substring(0, i);
            url = url.substring(i + 3);
        } else if ((i = url.indexOf(":/")) > 0) {
            protocol = url.substring(0, i);
            url = url.substring(i + 2);
        } else {
            throw new IllegalArgumentException("url " + url + " miss protocol!");
        }
        i = url.indexOf("/");
        if (i >= 0) {
            path = url.substring(i + 1);
            url = url.substring(0, i);
        }
        i = url.indexOf(":");
        if (i >= 0 && i < url.length() - 1) {
            port = Integer.parseInt(url.substring(i + 1));
            url = url.substring(0, i);
        }
        if (!url.isEmpty()) {
            host = url;
        }
        return new JRpcURL(protocol, host, port, path, parameters);
    }

    public JRpcURL deepClone() {
        if (parameters == null) {
            return new JRpcURL(getProtocol(), getHost(), getPort(), getPath());
        }
        return new JRpcURL(getProtocol(), getHost(), getPort(), getPath(), new HashMap<>(getParameters()));
    }

    public JRpcURL deepCloneWithParameter(Map<String, String> params) {
        JRpcURL url = deepClone();
        params.forEach(url::addParameter);
        return url;
    }

    public JRpcURL deepCloneWithAddress(String host, int port) {
        if (parameters == null) {
            return new JRpcURL(getProtocol(), host, port, getPath());
        }
        return new JRpcURL(getProtocol(), host, port, getPath(), new HashMap<>(getParameters()));
    }

    public String getUri() {
        return protocol + "://" + host + ":" + port + "/" + path;
    }

    public String getIdentity() {
        return getUri() +
                "/" + getGroup() +
                "/" + getVersion() +
                "/" + getNodeType();
    }

    public String toSimpleString() {
        return getUri() + "?group=" + getGroup() + "&version=" + getVersion();
    }

    public String toFullString() {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        builder.append(getUri());
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            if (first) {
                builder.append("?");
                first = false;
            } else {
                builder.append("&");
            }
            builder.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return builder.toString();
    }

    public String getAddress() {
        if (port <= 0) {
            return host;
        }
        int i = host.indexOf(":");
        if (i < 0) {
            return host + ":" + port;
        }
        int hostPort = Integer.parseInt(host.substring(i + 1));
        if (hostPort <= 0) {
            return host.substring(0, i + 1) + port;
        }
        return host;
    }

    public String getVersion() {
        return getParameter(JRpcURLParamType.version.getName(), JRpcURLParamType.version.getValue());
    }

    public String getGroup() {
        return getParameter(JRpcURLParamType.group.getName(), JRpcURLParamType.group.getValue());
    }

    public String getNodeType() {
        return getParameter(JRpcURLParamType.nodeType.getName(), JRpcURLParamType.nodeType.getValue());
    }

    public String getApplication() {
        return getParameter(JRpcURLParamType.application.getName(), JRpcURLParamType.application.getValue());
    }

    public String getModule() {
        return getParameter(JRpcURLParamType.module.getName(), JRpcURLParamType.module.getValue());
    }

    public String getProtocolKey() {
        return getProtocol() + "://" + getAddress() + "/" + getServiceKey();
    }

    public String getServiceKey() {
        return getGroup() + "/" + getPath() + "/" + getVersion();
    }

    public boolean containsParameter(String key) {
        return StringUtils.isNoneBlank(getParameter(key));
    }

    public String getParameter(String key) {
        return parameters.get(key);
    }

    public String getParameter(String key, String defaultValue) {
        String value = getParameter(key);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    public Boolean getParameter(String key, boolean defaultValue) {
        String value = getParameter(key);
        if (StringUtils.isEmpty(value)) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    public Integer getParameter(String key, int defaultValue) {
        Number number = getOrDefaultNumbers().get(key);
        if (number != null) {
            return number.intValue();
        }
        String value = getParameter(key);
        if (StringUtils.isEmpty(value)) {
            return defaultValue;
        }
        int ret = Integer.parseInt(value);
        getOrDefaultNumbers().put(key, ret);
        return ret;
    }

    public Long getParameter(String key, long defaultValue) {
        Number number = getOrDefaultNumbers().get(key);
        if (number != null) {
            return number.longValue();
        }
        String value = getParameter(key);
        if (StringUtils.isEmpty(value)) {
            return defaultValue;
        }
        long ret = Long.parseLong(value);
        getOrDefaultNumbers().put(key, ret);
        return ret;
    }

    public Float getParameter(String key, float defaultValue) {
        Number number = getOrDefaultNumbers().get(key);
        if (number != null) {
            return number.floatValue();
        }
        String value = getParameter(key);
        if (StringUtils.isEmpty(value)) {
            return defaultValue;
        }
        float ret = Float.parseFloat(value);
        getOrDefaultNumbers().put(key, ret);
        return ret;
    }

    public String getMethodParameter(String methodName, String paramDesc, String key) {
        String parameter = getParameter("methodconfig." + methodName + "(" + paramDesc + ")." + key);
        if (StringUtils.isEmpty(parameter)) {
            return getParameter(key);
        }
        return parameter;
    }

    public String getMethodParameter(String methodName, String paramDesc, String key, String defaultValue) {
        String parameter = getParameter("methodconfig." + methodName + "(" + paramDesc + ")." + key);
        if (StringUtils.isEmpty(parameter)) {
            return defaultValue;
        }
        return parameter;
    }

    public Boolean getMethodParameter(String methodName, String paramDesc, String key, boolean defaultValue) {
        String parameter = getMethodParameter(methodName, paramDesc, key);
        if (StringUtils.isEmpty(parameter)) {
            return defaultValue;
        }
        return Boolean.parseBoolean(parameter);
    }

    public Integer getMethodParameter(String methodName, String paramDesc, String key, int defaultValue) {
        String numKey = methodName + "(" + paramDesc + ")." + key;
        Number number = getOrDefaultNumbers().get(numKey);
        if (number != null) {
            return number.intValue();
        }
        String parameter = getMethodParameter(methodName, paramDesc, key);
        if (StringUtils.isEmpty(parameter)) {
            return defaultValue;
        }
        int ret = Integer.parseInt(parameter);
        getOrDefaultNumbers().put(numKey, ret);
        return ret;
    }

    public Long getMethodParameter(String methodName, String paramDesc, String key, long defaultValue) {
        String numKey = methodName + "(" + paramDesc + ")." + key;
        Number number = getOrDefaultNumbers().get(numKey);
        if (number != null) {
            return number.longValue();
        }
        String parameter = getMethodParameter(methodName, paramDesc, key);
        if (StringUtils.isEmpty(parameter)) {
            return defaultValue;
        }
        long ret = Long.parseLong(parameter);
        getOrDefaultNumbers().put(numKey, ret);
        return ret;
    }

    public Float getMethodParameter(String methodName, String paramDesc, String key, float defaultValue) {
        String numKey = methodName + "(" + paramDesc + ")." + key;
        Number number = getOrDefaultNumbers().get(numKey);
        if (number != null) {
            return number.floatValue();
        }
        String parameter = getMethodParameter(methodName, paramDesc, key);
        if (StringUtils.isEmpty(parameter)) {
            return defaultValue;
        }
        float ret = Float.parseFloat(parameter);
        getOrDefaultNumbers().put(numKey, ret);
        return ret;
    }

    public void addParameter(String key, String value) {
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(value)) {
            return;
        }
        parameters.put(key, value);
    }

    public void addParameterIfAbsent(String key, String value) {
        parameters.putIfAbsent(key, value);
    }

    public void addParameters(Map<String, String> map) {
        parameters.putAll(map);
    }

    public void removeParameter(String key) {
        if (key == null) {
            return;
        }
        parameters.remove(key);
    }

    public Map<String, Number> getOrDefaultNumbers() {
        if (cachedNumber == null) {
            cachedNumber = new ConcurrentHashMap<>();
        }
        return cachedNumber;
    }


    @Override
    public int hashCode() {
        int factor = 31;
        int hash = 1;
        hash = factor + hash + Objects.hashCode(protocol);
        hash = factor + hash + Objects.hashCode(host);
        hash = factor + hash + Objects.hashCode(port);
        hash = factor + hash + Objects.hashCode(path);
        hash = factor + hash + Objects.hashCode(parameters);
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JRpcURL jRpcURL = (JRpcURL) o;
        return port == jRpcURL.port &&
                Objects.equals(protocol, jRpcURL.protocol) &&
                Objects.equals(host, jRpcURL.host) &&
                Objects.equals(path, jRpcURL.path) &&
                Objects.equals(parameters, jRpcURL.parameters);
    }


    @Override
    public String toString() {
        return toSimpleString();
    }
}

