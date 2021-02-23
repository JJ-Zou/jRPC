package com.zjj.common;

public class BaseServiceMetadata {
    public static final char COLON_SEPERATOR = ':';
    protected String serviceKey;
    protected String serviceInterfaceName;
    protected String version;

    protected volatile String group;

    public static String buildServiceKey(String path, String group, String version) {
        StringBuilder buf = new StringBuilder();
        if (group != null && group.length() > 0) {
            buf.append(group).append("/");
        }
        buf.append(path);
        if (version != null && version.length() > 0) {
            buf.append(":").append(version);
        }
        return buf.toString();
    }


}

