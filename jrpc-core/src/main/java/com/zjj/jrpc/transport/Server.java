package com.zjj.jrpc.transport;

import java.net.InetSocketAddress;
import java.util.Collection;

public interface Server extends Endpoint {
    boolean isBound();

    Collection<TransChannel> getChannels();

    TransChannel getChannel(InetSocketAddress remoteAddress);

}
