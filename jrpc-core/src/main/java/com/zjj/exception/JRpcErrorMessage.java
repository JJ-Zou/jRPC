package com.zjj.exception;

public enum JRpcErrorMessage {
    SERVICE_DEFAULT_ERROR(10001, 503, "service error"),
    SERVICE_REJECT_ERROR(10002, 503, "service reject"),
    SERVICE_TIMEOUT_ERROR(10003, 503, "service request timeout"),
    SERVICE_TASK_CANCEL_ERROR(10004, 503, "service task cancel"),

    SERVICE_NOTFOUND_ERROR(10101, 404, "service not found"),

    SERVICE_REQUEST_LENGTH_OUT_OF_LIMIT_ERROR(10201, 403, "service request data length over of limit"),

    FRAMEWORK_DEFAULT_ERROR(20001, 503, "framework error"),
    FRAMEWORK_ENCODE_ERROR(20002, 503, "framework encode error"),
    FRAMEWORK_DECODE_ERROR(20003, 503, "framework decode error"),
    FRAMEWORK_INIT_ERROR(20004, 503, "framework init error"),
    FRAMEWORK_EXPORT_ERROR(20005, 503, "framework export error"),
    FRAMEWORK_SERVER_ERROR(20006, 503, "framework server error"),
    FRAMEWORK_REFER_ERROR(20007, 503, "framework reference error"),
    FRAMEWORK_REGISTER_ERROR(20008, 503, "framework register error"),

    PROVIDER_DEFAULT_ERROR(30001, 503, "provider error"),


    ;
    private final int status;
    private final int errorCode;
    private final String message;

    JRpcErrorMessage(int status, int errorCode, String message) {
        this.status = status;
        this.errorCode = errorCode;
        this.message = message;
    }


    public int getStatus() {
        return status;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "JRpcErrorMessage{" +
                "status=" + status +
                ", errorCode=" + errorCode +
                ", message='" + message + '\'' +
                '}';
    }
}
