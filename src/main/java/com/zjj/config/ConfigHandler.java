package com.zjj.config;

import com.zjj.clutter.Clutter;
import com.zjj.clutter.clutter.ClutterNotify;
import com.zjj.common.JRpcURL;
import com.zjj.rpc.Exporter;

import java.util.Collection;
import java.util.List;

public interface ConfigHandler {

    <T> ClutterNotify<T> getClutterNotify(Class<T> interfaceClass, Collection<JRpcURL> registryUrls, JRpcURL refUrl);

    <T> Exporter<T> export(Class<T> interfaceClass, T ref, Collection<JRpcURL> registryUrls, JRpcURL refUrl);

    <T> void unExport(Collection<Exporter<T>> exporters, Collection<JRpcURL> registryUrls);

    <T> T refer(Class<T> interfaceClass, List<Clutter<T>> clutters, String proxyType);
}
