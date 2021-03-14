package com.zjj.jrpc.exception;

public class JRpcServiceProviderException extends AbstractJRpcException {
    private static final long serialVersionUID = 5132650188529223634L;

    public JRpcServiceProviderException() {
        super(JRpcErrorMessage.SERVICE_DEFAULT_ERROR);
    }

    public JRpcServiceProviderException(JRpcErrorMessage errorMessage) {
        super(errorMessage);
    }

    public JRpcServiceProviderException(String message) {
        super(message, JRpcErrorMessage.SERVICE_DEFAULT_ERROR);
    }

    public JRpcServiceProviderException(Throwable cause) {
        super(cause, JRpcErrorMessage.SERVICE_DEFAULT_ERROR);
    }

    public JRpcServiceProviderException(String message, JRpcErrorMessage errorMessage) {
        super(message, errorMessage);
    }

    public JRpcServiceProviderException(String message, Throwable cause) {
        super(message, cause, JRpcErrorMessage.SERVICE_DEFAULT_ERROR);
    }

    public JRpcServiceProviderException(Throwable cause, JRpcErrorMessage errorMessage) {
        super(cause, errorMessage);
    }

    public JRpcServiceProviderException(String message, Throwable cause, JRpcErrorMessage errorMessage) {
        super(message, cause, errorMessage);
    }
}
