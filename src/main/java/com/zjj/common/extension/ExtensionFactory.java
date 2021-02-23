package com.zjj.common.extension;

public interface ExtensionFactory {
    <T> T getExtension(Class<T> type, String name);
}
