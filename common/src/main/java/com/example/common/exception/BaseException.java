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
     * Constructor that accepts an enum with error code and message
     * @param errorCodeEnum Enum containing error code and message
     * @param technicalMessage Technical details about the error
     */
    protected BaseException(Enum<?> errorCodeEnum, String technicalMessage) {
        super(technicalMessage);
        this.errorCode = ((Enum<?>) errorCodeEnum).name();
        this.userMessage = getEnumMessage(errorCodeEnum);
        this.technicalMessage = technicalMessage;
    }

    /**
     * Constructor that accepts an enum with error code and message
     * @param errorCodeEnum Enum containing error code and message
     * @param technicalMessage Technical details about the error
     * @param cause Original exception that caused this error
     */
    protected BaseException(Enum<?> errorCodeEnum, String technicalMessage, Throwable cause) {
        super(technicalMessage, cause);
        this.errorCode = ((Enum<?>) errorCodeEnum).name();
        this.userMessage = getEnumMessage(errorCodeEnum);
        this.technicalMessage = technicalMessage;
    }

    /**
     * Get message from enum using reflection
     * @param errorCodeEnum The enum constant
     * @return The message from enum or enum name if no message field exists
     */
    private String getEnumMessage(Enum<?> errorCodeEnum) {
        try {
            // Try to get 'message' field using reflection
            java.lang.reflect.Field messageField = errorCodeEnum.getClass().getDeclaredField("message");
            messageField.setAccessible(true);
            return (String) messageField.get(errorCodeEnum);
        } catch (Exception e) {
            // Fallback to toString() if no message field
            return errorCodeEnum.toString();
        }
    }

    /**
     * Get the gRPC status code for this exception
     * @return gRPC status code
     */
    public abstract io.grpc.Status.Code getGrpcStatusCode();
}
