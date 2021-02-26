package com.zjj.rpc.rpc;

import javax.naming.LimitExceededException;

public class RpcException extends RuntimeException {

    private static final long serialVersionUID = 2320751854901756257L;

    public static final int UNKNOWN_EXCEPTION = 0;
    public static final int NETWORK_EXCEPTION = 1;
    public static final int TIMEOUT_EXCEPTION = 2;
    public static final int BIZ_EXCEPTION = 3;
    public static final int FORBIDDEN_EXCEPTION = 4;
    public static final int SERIALIZATION_EXCEPTION = 5;
    public static final int NO_INVOKER_AVAILABLE_AFTER_FILTER = 6;
    public static final int LIMIT_EXCEEDED_EXCEPTION = 7;
    public static final int TIMEOUT_TERMINATE = 8;

    private int code;

    public RpcException() {
        super();
    }

    public RpcException(String message) {
        super(message);
    }

    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcException(Throwable cause) {
        super(cause);
    }

    public RpcException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public RpcException(int code) {
        super();
        this.code = code;
    }

    public RpcException(int code, String message) {
        super(message);
        this.code = code;
    }

    public RpcException(int code, Throwable cause) {
        super(cause);
        this.code = code;
    }

    public RpcException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public boolean isUnknown() {
        return code == UNKNOWN_EXCEPTION;
    }

    public boolean isNetwork() {
        return code == NETWORK_EXCEPTION;
    }

    public boolean isTimeout() {
        return code == TIMEOUT_EXCEPTION;
    }

    public boolean isBiz() {
        return code == BIZ_EXCEPTION;
    }

    public boolean isForbidden() {
        return code == FORBIDDEN_EXCEPTION;
    }

    public boolean isSerialization() {
        return code == SERIALIZATION_EXCEPTION;
    }

    public boolean isNoInvokerAvailableAfterFilter() {
        return code == NO_INVOKER_AVAILABLE_AFTER_FILTER;
    }


    public boolean isTimeoutTerminate() {
        return code == TIMEOUT_TERMINATE;
    }

    public boolean isLimitExceeded() {
        return code == LIMIT_EXCEEDED_EXCEPTION || (getCause() instanceof LimitExceededException);
    }
}
