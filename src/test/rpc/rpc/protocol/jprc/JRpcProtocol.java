package com.zjj.rpc.rpc.protocol.jprc;

import com.zjj.rpc.remoting.RemotingException;
import com.zjj.rpc.remoting.RemotingServer;
import com.zjj.rpc.remoting.transport.netty.NettyTransporter;
import com.zjj.rpc.rpc.Exporter;
import com.zjj.rpc.rpc.ProtocolServer;
import com.zjj.rpc.rpc.RpcException;
import com.zjj.rpc.rpc.protocol.AbstractProtocol;


public class JRpcProtocol extends AbstractProtocol {
    private static final String NAME = "jRpc";

    @Override
    public <T> Exporter<T> export() throws RpcException {
        openServer();
        return null;
    }


    private void openServer() {
        serverMap.put("address", createServer());
    }

    private ProtocolServer createServer() {
        NettyTransporter nettyTransporter = new NettyTransporter();
        RemotingServer server;
        try {
            server = nettyTransporter.bind();
        } catch (RemotingException e) {
            throw new RpcException("Fail to start server." + e.getMessage(), e);
        }
        return new JRpcProtocolServer(server);
    }
}
