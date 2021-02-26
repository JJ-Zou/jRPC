package com.zjj.rpc.remoting;

public interface Transporter {
    RemotingServer bind() throws RemotingException;
}
