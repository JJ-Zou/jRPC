package com.zjj.rpc.remoting;

import io.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class RemotingException extends Exception {
    private static final long serialVersionUID = -3509387900578465251L;
    private InetSocketAddress localAddress;

    private InetSocketAddress remoteAddress;

    public RemotingException(Channel channel, String msg) {
        this(channel == null ? null : channel.localAddress(), channel == null ? null : channel.remoteAddress(), msg);
    }

    public RemotingException(SocketAddress localAddress, SocketAddress remoteAddress, String message) {
        super(message);
        this.localAddress = (InetSocketAddress) localAddress;
        this.remoteAddress = (InetSocketAddress) remoteAddress;
    }

    public RemotingException(Channel channel, Throwable cause) {
        this(channel == null ? null : channel.localAddress(), channel == null ? null : channel.remoteAddress(), cause);
    }

    public RemotingException(SocketAddress localAddress, SocketAddress remoteAddress, Throwable cause) {
        super(cause);
        this.localAddress = (InetSocketAddress) localAddress;
        this.remoteAddress = (InetSocketAddress) remoteAddress;
    }

    public RemotingException(Channel channel, String message, Throwable cause) {
        this(channel == null ? null : channel.localAddress(), channel == null ? null : channel.remoteAddress(), message, cause);
    }

    public RemotingException(SocketAddress localAddress, SocketAddress remoteAddress, String message,
                             Throwable cause) {
        super(message, cause);
        this.localAddress = (InetSocketAddress) localAddress;
        this.remoteAddress = (InetSocketAddress) remoteAddress;
    }

    public InetSocketAddress localAddress() {
        return localAddress;
    }

    public InetSocketAddress remoteAddress() {
        return remoteAddress;
    }
}
