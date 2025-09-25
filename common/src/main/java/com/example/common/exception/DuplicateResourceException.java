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

    @Override
    public io.grpc.Status.Code getGrpcStatusCode() {
        return Status.Code.ALREADY_EXISTS;
    }
}
