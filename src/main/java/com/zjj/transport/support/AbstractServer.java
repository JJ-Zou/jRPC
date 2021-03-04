package com.zjj.transport.support;

import com.zjj.common.JRpcURL;
import com.zjj.transport.Server;
import com.zjj.transport.TransChannel;

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

    @Override
    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    public void setLocalAddress(InetSocketAddress localAddress) {
        this.localAddress = localAddress;
    }

    public void setRemoteAddress(InetSocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    @Override
    public Collection<TransChannel> getChannels() {
        throw new IllegalStateException(this.getClass().getName() + " method getChannels unsupported " + url);
    }

    @Override
    public TransChannel getChannel(InetSocketAddress remoteAddress) {
        throw new IllegalStateException(this.getClass().getName() + " method getChannel(InetSocketAddress) unsupported " + url);
    }
}
