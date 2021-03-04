package com.zjj.rpc.rpc.protocol.jprc;

import com.zjj.rpc.remoting.RemotingServer;
import com.zjj.rpc.rpc.ProtocolServer;

public class JRpcProtocolServer implements ProtocolServer {

    private RemotingServer server;
    private String address;

    public JRpcProtocolServer(RemotingServer server) {
        this.server = server;
    }

    @Override
    public RemotingServer getRemotingServer() {
        return server;
    }


    @Override
    public String getAddress() {
        return null;
    }

    @Override
    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public void close() {

    }
}
