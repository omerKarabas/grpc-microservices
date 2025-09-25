package com.example.common.exception;

import lombok.Getter;

/**
 * Base exception class for all custom exceptions
 * Provides common properties and behavior
 */
@Getter
public abstract class BaseException extends RuntimeException {

    private final String errorCode;
    private final String userMessage;
    private final String technicalMessage;

    protected BaseException(String errorCode, String userMessage, String technicalMessage) {
        super(technicalMessage);
        this.errorCode = errorCode;
        this.userMessage = userMessage;
        this.technicalMessage = technicalMessage;
    }

    protected BaseException(String errorCode, String userMessage, String technicalMessage, Throwable cause) {
        super(technicalMessage, cause);
        this.errorCode = errorCode;
        this.userMessage = userMessage;
        this.technicalMessage = technicalMessage;
    }

    /**
     * Get the gRPC status code for this exception
     * @return gRPC status code
     */
    public abstract io.grpc.Status.Code getGrpcStatusCode();
}
