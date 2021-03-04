package com.zjj.rpc.remoting.transport;

import com.zjj.rpc.remoting.RemotingServer;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public abstract class AbstractServer implements RemotingServer {

    private InetSocketAddress localAddress;
    private InetSocketAddress bindAddress;

    protected AbstractServer() {
        localAddress = new InetSocketAddress("169.254.11.10", 10520);
        bindAddress = new InetSocketAddress(10520);
        try {
            doOpen();
            log.info("Start {} bind {}, export {}", getClass().getSimpleName(), getBindAddress(), getLocalAddress());
        } catch (Throwable t) {
            throw new RuntimeException();
        }
    }

    protected abstract void doOpen();

    public InetSocketAddress getBindAddress() {
        return bindAddress;
    }

    public InetSocketAddress getLocalAddress() {
        return localAddress;
    }

}
