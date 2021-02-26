package com.zjj.rpc.remoting;

import io.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.util.Collection;

public interface RemotingServer {
    boolean isBound();

    Collection<Channel> getChannels();

    Channel getChannel(InetSocketAddress remotingAddress);
}

