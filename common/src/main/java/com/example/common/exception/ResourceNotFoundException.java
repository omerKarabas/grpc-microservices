package com.example.common.exception;

import io.grpc.Status;

/**
 * Exception for resource not found scenarios
 * Maps to gRPC NOT_FOUND status
 */
public class ResourceNotFoundException extends BaseException {

    public ResourceNotFoundException(String errorCode, String userMessage, String technicalMessage) {
        super(errorCode, userMessage, technicalMessage);
    }

    public ResourceNotFoundException(String errorCode, String userMessage, String technicalMessage, Throwable cause) {
        super(errorCode, userMessage, technicalMessage, cause);
    }

    @Override
    public io.grpc.Status.Code getGrpcStatusCode() {
        return Status.Code.NOT_FOUND;
    }
}
