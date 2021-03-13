package com.zjj.exception;

import com.zjj.rpc.context.RpcContext;

public abstract class AbstractJRpcException extends RuntimeException {
    private static final long serialVersionUID = -8738228182749237357L;
    protected JRpcErrorMessage errorMessage;

    protected AbstractJRpcException() {
    }

    protected AbstractJRpcException(JRpcErrorMessage errorMessage) {
        this.errorMessage = errorMessage;
    }

    protected AbstractJRpcException(String message) {
        super(message);
    }

    protected AbstractJRpcException(Throwable cause) {
        super(cause);
    }

    protected AbstractJRpcException(String message, JRpcErrorMessage errorMessage) {
        super(message);
        this.errorMessage = errorMessage;
    }

    protected AbstractJRpcException(String message, Throwable cause) {
        super(message, cause);
    }

    protected AbstractJRpcException(Throwable cause, JRpcErrorMessage errorMessage) {
        super(cause);
        this.errorMessage = errorMessage;
    }

    protected AbstractJRpcException(String message, Throwable cause, JRpcErrorMessage errorMessage) {
        super(message, cause);
        this.errorMessage = errorMessage;
    }


    @Override
    public String getMessage() {
        String errMsg = "";
        if (errorMessage != null) {
            errMsg = errorMessage.toString();
        }
        return errMsg + ", id = " + RpcContext.getRpcContext().getRequestId();
    }

    public JRpcErrorMessage getErrorMessage() {
        return errorMessage;
    }
}
