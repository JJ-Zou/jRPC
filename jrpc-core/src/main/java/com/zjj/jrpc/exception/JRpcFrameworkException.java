package com.zjj.jrpc.exception;

public class JRpcFrameworkException extends AbstractJRpcException {
    private static final long serialVersionUID = -1913993355270838845L;

    public JRpcFrameworkException() {
        super(JRpcErrorMessage.FRAMEWORK_DEFAULT_ERROR);
    }

    public JRpcFrameworkException(JRpcErrorMessage errorMessage) {
        super(errorMessage);
    }

    public JRpcFrameworkException(String message) {
        super(message, JRpcErrorMessage.FRAMEWORK_DEFAULT_ERROR);
    }

    public JRpcFrameworkException(Throwable cause) {
        super(cause, JRpcErrorMessage.FRAMEWORK_DEFAULT_ERROR);
    }

    public JRpcFrameworkException(String message, JRpcErrorMessage errorMessage) {
        super(message, errorMessage);
    }

    public JRpcFrameworkException(String message, Throwable cause) {
        super(message, cause, JRpcErrorMessage.FRAMEWORK_DEFAULT_ERROR);
    }

    public JRpcFrameworkException(Throwable cause, JRpcErrorMessage errorMessage) {
        super(cause, errorMessage);
    }

    public JRpcFrameworkException(String message, Throwable cause, JRpcErrorMessage errorMessage) {
        super(message, cause, errorMessage);
    }
}
