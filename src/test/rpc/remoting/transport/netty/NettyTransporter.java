package com.zjj.rpc.remoting.transport.netty;

import com.zjj.rpc.remoting.RemotingException;
import com.zjj.rpc.remoting.RemotingServer;
import com.zjj.rpc.remoting.Transporter;

public class NettyTransporter implements Transporter {
    @Override
    public RemotingServer bind() throws RemotingException {
        return new NettyServer();
    }
}
