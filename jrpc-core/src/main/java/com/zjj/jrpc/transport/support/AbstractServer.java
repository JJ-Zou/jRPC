package com.zjj.jrpc.transport.support;

import com.zjj.jrpc.common.JRpcURL;
import com.zjj.jrpc.exception.JRpcFrameworkException;
import com.zjj.jrpc.transport.Server;
import com.zjj.jrpc.transport.TransChannel;

import java.net.InetSocketAddress;
import java.util.Collection;

public abstract class AbstractServer implements Server {
    protected InetSocketAddress localAddress;
    protected InetSocketAddress remoteAddress;

    protected JRpcURL url;

    protected AbstractServer(JRpcURL url) {
        this.url = url;
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return localAddress;
    }

    public void setLocalAddress(InetSocketAddress localAddress) {
        this.localAddress = localAddress;
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(InetSocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    @Override
    public Collection<TransChannel> getChannels() {
        throw new JRpcFrameworkException(this.getClass().getName() + " method getChannels unsupported " + url);
    }

    @Override
    public TransChannel getChannel(InetSocketAddress remoteAddress) {
        throw new JRpcFrameworkException(this.getClass().getName() + " method getChannel(InetSocketAddress) unsupported " + url);
    }
}
