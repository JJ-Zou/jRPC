package com.zjj.common;

import com.zjj.common.constants.CommonConstants;
import com.zjj.common.utils.ArrayUtils;
import com.zjj.common.utils.CollectionUtils;
import com.zjj.common.utils.NetUtils;
import com.zjj.common.utils.StringUtils;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.zjj.common.RemotingConstants.BACKUP_KEY;

public class URL implements Serializable {
    private static final long serialVersionUID = 5092896872143624146L;
    // url协议
    private final String protocol;
    private final String username;
    private final String password;
    private final String host;
    private final int port;
    // 接口名称
    private final String path;
    private final Map<String, String> parameters;
    private final Map<String, Map<String, String>> methodParameters;

    private volatile transient Map<String, Number> numbers;
    // ip缓存
    private volatile transient String ip;
    // toString缓存
    private volatile transient String string;

    private transient String serviceKey;
    private transient String address;

    protected URL() {
        this.protocol = null;
        this.username = null;
        this.password = null;
        this.host = null;
        this.port = 0;
        this.path = null;
        this.parameters = null;
        this.methodParameters = null;
    }

    public URL(String protocol,
               String host,
               int port) {
        this(protocol, null, null, host, port, null, (Map<String, String>) null);
    }

    public URL(String protocol,
               String host,
               int port,
               String[] pairs) {
        this(protocol, null, null, host, port, null, pairs);
    }

    public URL(String protocol,
               String host,
               int port,
               Map<String, String> parameters) {
        this(protocol, null, null, host, port, null, parameters);
    }

    public URL(String protocol,
               String host,
               int port,
               String path) {
        this(protocol, null, null, host, port, path, (Map<String, String>) null);
    }

    public URL(String protocol,
               String host,
               int port,
               String path,
               String... pairs) {
        this(protocol, null, null, host, port, path, pairs);
    }

    public URL(String protocol,
               String host,
               int port,
               String path,
               Map<String, String> parameters) {
        this(protocol, null, null, host, port, path, parameters);
    }

    public URL(String protocol,
               String username,
               String password,
               String host,
               int port,
               String path) {
        this(protocol, username, password, host, port, path, (Map<String, String>) null);
    }

    public URL(String protocol,
               String username,
               String password,
               String host,
               int port,
               String path,
               String... pairs) {
        this(protocol, username, password, host, port, path, CollectionUtils.toStringMap(pairs));
    }

    public URL(String protocol,
               String username,
               String password,
               String host,
               int port,
               String path,
               Map<String, String> parameters) {
        this(protocol, username, password, host, port, path, parameters, toMethodParameters(parameters));
    }

    public URL(String protocol,
               String username,
               String password,
               String host,
               int port,
               String path,
               Map<String, String> parameters,
               Map<String, Map<String, String>> methodParameters) {
        if (StringUtils.isEmpty(username) && StringUtils.isNotEmpty(password)) {
            throw new IllegalArgumentException("Invalid url, password without username!");
        }
        this.protocol = protocol;
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = Math.max(0, port);
        this.address = getAddress(this.host, this.port);
        // 去掉前置 "/"
        while (path != null && path.startsWith("/")) {
            path = path.substring(1);
        }
        this.path = path;
        if (parameters == null) {
            parameters = new HashMap<>();
        } else {
            parameters = new HashMap<>(parameters);
        }
        this.parameters = Collections.unmodifiableMap(parameters);
        this.methodParameters = Collections.unmodifiableMap(methodParameters);
    }

    public static URL valueOf(String url) {
        if (url == null || (url = url.trim()).length() == 0) {
            throw new IllegalArgumentException("url == null");
        }
        String protocol = null;
        String username = null;
        String password = null;
        String host = null;
        int port = 0;
        String path = null;
        Map<String, String> parameters = null;
        int i = url.indexOf('?');
        // 转换parameters
        if (i >= 0) {
            String[] pairs = url.substring(i + 1).split("&");
            parameters = new HashMap<>();
            for (String pair : pairs) {
                pair = pair.trim();
                if (pair.length() > 0) {
                    int j = pair.indexOf('=');
                    if ((j >= 0)) {
                        String key = pair.substring(0, j);
                        String value = pair.substring(j + 1);
                        parameters.put(key, value);
                        if (key.startsWith(CommonConstants.DEFAULT_KEY_PREFIX)) {
                            parameters.putIfAbsent(key.substring(CommonConstants.DEFAULT_KEY_PREFIX.length()), value);
                        }
                    } else {
                        parameters.put(pair, pair);
                    }
                }
                url = url.substring(0, i);
            }
        }
        // 读取协议
        i = url.indexOf("://");
        if (i >= 0) {
            if (i == 0) {
                throw new IllegalStateException("url missing protocol: \"" + url + "\"");
            }
            protocol = url.substring(0, i);
            url = url.substring(i + 3);
        } else {
            // 单/
            i = url.indexOf(":/");
            if (i >= 0) {
                if (i == 0) {
                    throw new IllegalStateException("url missing protocol: \"" + url + "\"");
                }
                protocol = url.substring(0, i);
                url = url.substring(i + 1);
            }
        }
        // 读取path
        i = url.indexOf('/');
        if (i >= 0) {
            path = url.substring(i + 1);
            url = url.substring(0, i);
        }
        // 读取用户名和密码
        i = url.lastIndexOf('@');
        if (i >= 0) {
            username = url.substring(0, i);
            int j = username.indexOf(':');
            if (j >= 0) {
                username = username.substring(0, j);
                password = username.substring(j + 1);
            }
            url = url.substring(i + 1);
        }
        i = url.lastIndexOf(':');
        if (i >= 0 && i < url.length() - 1) {
            if (url.lastIndexOf('%') > i) {
                // ipv6 address with scope id
                // e.g. fe80:0:0:0:894:aeec:f37d:23e1%en0
                // see https://howdoesinternetwork.com/2013/ipv6-zone-id
                // ignore
            } else {
                port = Integer.parseInt(url.substring(i + 1));
                url = url.substring(0, i);
            }
        }
        if (url.length() > 0) {
            host = url;
        }
        return new URL(protocol, username, password, host, port, path, parameters);
    }

    public static String encode(String value) {
        if (StringUtils.isEmpty(value)) {
            return "";
        }
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public String getAddress() {
        if (address == null) {
            address = getAddress(host, port);
        }
        return address;
    }

    private static String getAddress(String host, int port) {
        return port <= 0 ? host : host + ":" + port;
    }

    public String getBackupAddress() {
        return getBackupAddress(0);
    }

    public String getBackupAddress(int defaultPort) {
        StringBuilder address = new StringBuilder(appendDefaultPort(getAddress(), defaultPort));
        String[] backups = getParameter(BACKUP_KEY, new String[0]);
        if (ArrayUtils.isNotEmpty(backups)) {
            for (String backup : backups) {
                address.append(',').append(appendDefaultPort(backup, defaultPort));
            }
        }
        return address.toString();
    }

    static String appendDefaultPort(String address, int defaultPort) {
        if (address != null && address.length() > 0 && defaultPort > 0) {
            int i = address.indexOf(':');
            if (i < 0) {
                return address + ":" + defaultPort;
            } else if (Integer.parseInt(address.substring(i + 1)) == 0) {
                return address.substring(0, i + 1) + defaultPort;
            }
        }
        return address;
    }

    private static Map<String, Map<String, String>> toMethodParameters(Map<String, String> parameters) {
        Map<String, Map<String, String>> methodParameters = new HashMap<>();
        if (parameters == null) {
            return methodParameters;
        }
        String methodsString = parameters.get(CommonConstants.METHODS_KEY);
        if (StringUtils.isNotEmpty(methodsString)) {
            List<String> methods = StringUtils.splitToList(methodsString, ',');
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                String key = entry.getKey();
                for (String method : methods) {
                    int methodLen = method.length();
                    // key 的格式为 method.parameters
                    if (key.length() > methodLen && key.startsWith(method) && key.charAt(methodLen) == '.') {
                        String realKey = key.substring(methodLen + 1);
                        putMethodParameter(method, realKey, entry.getValue(), methodParameters);
                    }
                }
            }
        } else {
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                String key = entry.getKey();
                int methodSeparator = key.indexOf('.');
                if (methodSeparator > 0) {
                    String method = key.substring(0, methodSeparator);
                    String realKey = key.substring(methodSeparator + 1);
                    putMethodParameter(method, realKey, entry.getValue(), methodParameters);
                }
            }
        }
        return methodParameters;
    }

    public static void putMethodParameter(String method, String key, String value, Map<String, Map<String, String>> methodParameters) {
        Map<String, String> subParameter = methodParameters.computeIfAbsent(method, k -> new HashMap<>());
        subParameter.put(key, value);
    }

    public String getProtocol() {
        return protocol;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
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

    public Map<String, String> getParameters() {
        return parameters;
    }


    @Override
    public String toString() {
        if (string != null) {
            return string;
        }
        string = buildString(false, true);
        return string;
    }

    private String buildString(boolean appendUser, boolean appendParameter, String... parameters) {
        return buildString(appendUser, appendParameter, false, false, parameters);
    }

    private String buildString(boolean appendUser, boolean appendParameter, boolean useIp, boolean useService, String... parameters) {
        StringBuffer buf = new StringBuffer();
        if (StringUtils.isNotEmpty(protocol)) {
            buf.append(protocol).append("://");
        }
        if (appendUser && StringUtils.isNotEmpty(username)) {
            buf.append(username);
            if (StringUtils.isNotEmpty(password)) {
                buf.append(":").append(password);
            }
            buf.append("@");
        }
        String host;
        if (useIp) {
            host = getIp();
        } else {
            host = getHost();
        }
        if (StringUtils.isNotEmpty(host)) {
            buf.append(host);
            if (port > 0) {
                buf.append(":").append(port);
            }
        }
        String path;
        if (useService) {
            path = getServiceKey();
        } else {
            path = getPath();
        }
        if (StringUtils.isNotEmpty(path)) {
            buf.append("/").append(path);
        }
        if (appendParameter) {
            buildParameters(buf, true, parameters);
        }
        return buf.toString();
    }

    public String toServiceStringWithoutResolving() {
        return buildString(true, false, false, true);
    }

    private void buildParameters(StringBuffer buf, boolean concat, String[] parameters) {
        if (CollectionUtils.isEmptyMap(getParameters())) {
            return;
        }
        List<String> includes = (ArrayUtils.isEmpty(parameters)) ? null : Arrays.asList(parameters);
        boolean first = true;
        for (Map.Entry<String, String> entry : new TreeMap<>(getParameters()).entrySet()) {
            if (StringUtils.isNotEmpty(entry.getKey()) && (includes == null || includes.contains(entry.getKey()))) {
                if (first) {
                    if (concat) {
                        buf.append("?");
                    }
                    first = false;
                } else {
                    buf.append("&");
                }
                buf.append(entry.getKey()).append("=").append(entry.getValue() == null ? "" : entry.getValue().trim());
            }
        }
    }

    public String getServiceKey() {
        if (serviceKey != null) {
            return serviceKey;
        }
        String inf = getServiceInterface();
        if (inf == null) {
            return null;
        }
        serviceKey = buildKey(inf, getParameter(CommonConstants.GROUP_KEY), getParameter(CommonConstants.VERSION_KEY));
        return serviceKey;
    }

    private String buildKey(String path, String group, String version) {
        return BaseServiceMetadata.buildServiceKey(path, group, version);
    }

    public String getServiceInterface() {
        return getParameter(CommonConstants.INTERFACE_KEY, path);
    }

    private Map<String, Number> getNumbers() {
        if (numbers == null) {
            numbers = new ConcurrentHashMap<>();
        }
        return numbers;
    }

    public boolean getParameter(String key, boolean defaultValue) {
        String value = getParameter(key);
        return StringUtils.isEmpty(value) ? defaultValue : Boolean.parseBoolean(value);
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

    public String getParameter(String key, String defaultValue) {
        String value = getParameter(key);
        return StringUtils.isEmpty(value) ? defaultValue : value;
    }


    public String getParameter(String key) {
        return parameters.get(key);
    }

    public String[] getParameter(String key, String[] defaultValue) {
        String value = getParameter(key);
        return StringUtils.isEmpty(value) ? defaultValue : CommonConstants.COMMA_SPLIT_PATTERN.split(value);
    }

    public Collection<String> getParameter(String key, List<String> defaultValue) {
        String value = getParameter(key);
        if (StringUtils.isEmpty(key)) {
            return defaultValue;
        }
        String[] split = CommonConstants.COMMA_SPLIT_PATTERN.split(value);
        return Arrays.asList(split);
    }

    public String getIp() {
        if (ip == null) {
            ip = NetUtils.getIpByHost(host);
        }
        return ip;
    }

    public boolean isAnyHost() {
        return CommonConstants.ANYHOST_VALUE.equals(host) || getParameter(CommonConstants.ANYHOST_KEY, false);
    }


    public String getAuthority() {
        if (StringUtils.isEmpty(username) && StringUtils.isEmpty(password)) {
            return null;
        }
        return (username == null ? "" : username) + ":" + (password == null ? "" : password);
    }
}
