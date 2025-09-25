package com.example.common.exception;

import io.grpc.Status;

/**
 * Exception for input validation failures
 * Maps to gRPC INVALID_ARGUMENT status
 */
public class ValidationException extends BaseException {

    public ValidationException(String errorCode, String userMessage, String technicalMessage) {
        super(errorCode, userMessage, technicalMessage);
    }

    public ValidationException(String errorCode, String userMessage, String technicalMessage, Throwable cause) {
        super(errorCode, userMessage, technicalMessage, cause);
    }

    @Override
    public io.grpc.Status.Code getGrpcStatusCode() {
        return Status.Code.INVALID_ARGUMENT;
    }
}
