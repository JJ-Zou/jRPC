package com.zjj.rpc.rpc;

import com.zjj.rpc.remoting.RemotingServer;

public interface ProtocolServer {
    String getAddress();

    void setAddress(String address);

    void close();

    default RemotingServer getRemotingServer() {
        return null;
    }

    default void setRemotingServer(RemotingServer server) {
    }
}
