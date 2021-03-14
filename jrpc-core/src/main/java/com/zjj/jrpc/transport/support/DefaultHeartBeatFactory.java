package com.zjj.jrpc.transport.support;

import com.zjj.jrpc.common.utils.RequestIdUtils;
import com.zjj.jrpc.rpc.Request;
import com.zjj.jrpc.rpc.Response;
import com.zjj.jrpc.rpc.message.DefaultRequest;
import com.zjj.jrpc.rpc.message.DefaultResponse;
import com.zjj.jrpc.transport.HeartBeatFactory;
import com.zjj.jrpc.transport.MessageHandler;
import com.zjj.jrpc.transport.TransChannel;

public class DefaultHeartBeatFactory implements HeartBeatFactory {
    public static Request createHeartBeatRequest(long requestId) {
        HeartBeatRequest request = new HeartBeatRequest();
        request.setRequestId(requestId);
        request.setInterfaceName(HeartBeatRequest.class.getName());
        request.setMethodName("heartbeat");
        request.setParameterSign("void");
        return request;
    }

    public static Response createHeartBeatResponse(Request request) {
        HeartBeatResponse response = new HeartBeatResponse();
        response.setRequestId(request.getRequestId());
        response.setValue("heartbeat");
        response.setProtocolVersion(request.getProtocolVersion());
        return response;
    }

    @Override
    public Request createRequest() {
        return createHeartBeatRequest(RequestIdUtils.getRequestId());
    }

    @Override
    public MessageHandler wrap(MessageHandler handler) {
        return new Wrapper(handler);
    }

    private static class Wrapper implements MessageHandler {
        private final MessageHandler messageHandler;

        public Wrapper(MessageHandler messageHandler) {
            this.messageHandler = messageHandler;
        }

        @Override
        public Object handler(TransChannel transChannel, Object message) {
            if (!(message instanceof HeartBeatRequest)) {
                return messageHandler.handler(transChannel, message);
            }
            Request request = (Request) message;
            return createHeartBeatResponse(request);
        }
    }

    public static class HeartBeatRequest extends DefaultRequest {
        private static final long serialVersionUID = 4702210210433150245L;
    }

    public static class HeartBeatResponse extends DefaultResponse {
        private static final long serialVersionUID = 6393883712923140959L;
    }
}
