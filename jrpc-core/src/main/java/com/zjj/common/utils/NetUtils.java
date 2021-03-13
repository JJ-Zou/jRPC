package com.zjj.common.utils;

import com.zjj.common.JRpcURLParamType;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.stream.Stream;

public class NetUtils {
    private static InetAddress address;

    private NetUtils() {
    }

    public static String getRealLocalHostString() {
        address = getRealLocalHost();
        return address == null ? JRpcURLParamType.LOCALHOST.getValue() : address.getHostAddress();
    }

    public static InetAddress getRealLocalHost() {
        try {
            return Collections.list(NetworkInterface.getNetworkInterfaces()).stream()
                    .filter(networkInterface -> {
                        try {
                            return !networkInterface.isVirtual() && !networkInterface.isLoopback()
                                    && networkInterface.isUp() && !networkInterface.getName().contains("docker");
                        } catch (SocketException socketException) {
                            return false;
                        }
                    }).findAny()
                    .map(networkInterface -> Collections.list(networkInterface.getInetAddresses()).stream())
                    .flatMap(Stream::findFirst)
                    .orElse(InetAddress.getLocalHost());
        } catch (SocketException | UnknownHostException e) {
            return null;
        }
    }

    public static String getLocalHostString() {
        address = getLocalHost();
        return address == null ? JRpcURLParamType.LOCALHOST.getValue() : address.getHostAddress();
    }

    public static InetAddress getLocalHost() {
        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            return getRealLocalHost();
        }
    }

    public static String getLoopbackIp() {
        return InetAddress.getLoopbackAddress().getHostAddress();
    }
}
