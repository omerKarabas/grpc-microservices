package com.example.common.exception;

import io.grpc.Status;

/**
 * Exception for duplicate resource scenarios (e.g., duplicate email)
 * Maps to gRPC ALREADY_EXISTS status
 */
public class DuplicateResourceException extends BaseException {

    public DuplicateResourceException(String errorCode, String userMessage, String technicalMessage) {
        super(errorCode, userMessage, technicalMessage);
    }

    public DuplicateResourceException(String errorCode, String userMessage, String technicalMessage, Throwable cause) {
        super(errorCode, userMessage, technicalMessage, cause);
    }

    /**
     * Constructor that accepts an error code enum
     * @param errorCodeEnum Enum containing error code and message
     * @param technicalMessage Technical details about the error
     */
    public DuplicateResourceException(Enum<?> errorCodeEnum, String technicalMessage) {
        super(errorCodeEnum, technicalMessage);
    }

    /**
     * Constructor that accepts an error code enum with cause
     * @param errorCodeEnum Enum containing error code and message
     * @param technicalMessage Technical details about the error
     * @param cause Original exception that caused this error
     */
    public DuplicateResourceException(Enum<?> errorCodeEnum, String technicalMessage, Throwable cause) {
        super(errorCodeEnum, technicalMessage, cause);
    }

    @Override
    public io.grpc.Status.Code getGrpcStatusCode() {
        return Status.Code.ALREADY_EXISTS;
    }
}
