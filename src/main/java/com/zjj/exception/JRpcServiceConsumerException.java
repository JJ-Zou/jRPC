package com.zjj.exception;

public class JRpcServiceConsumerException extends AbstractJRpcException{
    private static final long serialVersionUID = -1043010265810715053L;

    public JRpcServiceConsumerException() {
        super(JRpcErrorMessage.PROVIDER_DEFAULT_ERROR);
    }

    public JRpcServiceConsumerException(JRpcErrorMessage errorMessage) {
        super(errorMessage);
    }

    public JRpcServiceConsumerException(String message) {
        super(message, JRpcErrorMessage.PROVIDER_DEFAULT_ERROR);
    }

    public JRpcServiceConsumerException(Throwable cause) {
        super(cause,JRpcErrorMessage.PROVIDER_DEFAULT_ERROR);
    }

    public JRpcServiceConsumerException(String message, JRpcErrorMessage errorMessage) {
        super(message, errorMessage);
    }

    public JRpcServiceConsumerException(String message, Throwable cause) {
        super(message, cause, JRpcErrorMessage.PROVIDER_DEFAULT_ERROR);
    }

    public JRpcServiceConsumerException(Throwable cause, JRpcErrorMessage errorMessage) {
        super(cause, errorMessage);
    }

    public JRpcServiceConsumerException(String message, Throwable cause, JRpcErrorMessage errorMessage) {
        super(message, cause, errorMessage);
    }
}
