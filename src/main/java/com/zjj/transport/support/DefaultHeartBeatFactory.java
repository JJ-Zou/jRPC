package com.zjj.transport.support;

import com.zjj.common.utils.RequestIdUtils;
import com.zjj.rpc.Request;
import com.zjj.rpc.Response;
import com.zjj.rpc.message.DefaultRequest;
import com.zjj.rpc.message.DefaultResponse;
import com.zjj.transport.HeartBeatFactory;
import com.zjj.transport.MessageHandler;
import com.zjj.transport.TransChannel;

public class DefaultHeartBeatFactory implements HeartBeatFactory {
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

    static class HeartBeatRequest extends DefaultRequest {
        private static final long serialVersionUID = 4702210210433150245L;
    }

    static class HeartBeatResponse extends DefaultResponse {
        private static final long serialVersionUID = 6393883712923140959L;
    }
}
