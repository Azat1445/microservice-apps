package org.example.orderservice.exception;

public class GrpcServiceUnavailableException extends RuntimeException {
    public GrpcServiceUnavailableException(String msg) {
        super(msg);
    }

    public GrpcServiceUnavailableException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
