package com.example.common.grpc;

import com.example.common.exception.BaseException;
import com.example.common.exception.BusinessException;
import com.example.common.exception.DuplicateResourceException;
import com.example.common.exception.ResourceNotFoundException;
import com.example.common.exception.ValidationException;
import io.grpc.*;
import lombok.extern.slf4j.Slf4j;

/**
 * Global gRPC interceptor for centralized exception handling
 * Catches all exceptions from gRPC service methods and converts them to appropriate gRPC status
 */
@Slf4j
public class GlobalExceptionInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> serverCall,
            Metadata metadata,
            ServerCallHandler<ReqT, RespT> serverCallHandler) {

        ServerCall.Listener<ReqT> listener = serverCallHandler.startCall(serverCall, metadata);

        return new ExceptionHandlingListener<>(listener, serverCall, metadata);
    }

    /**
     * Wrapper listener that handles exceptions from the original listener
     */
    private static class ExceptionHandlingListener<ReqT> extends ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT> {

        private final ServerCall<ReqT, ?> serverCall;
        private final Metadata metadata;

        public ExceptionHandlingListener(ServerCall.Listener<ReqT> delegate, ServerCall<ReqT, ?> serverCall, Metadata metadata) {
            super(delegate);
            this.serverCall = serverCall;
            this.metadata = metadata;
        }

        @Override
        public void onMessage(ReqT message) {
            try {
                super.onMessage(message);
            } catch (Exception e) {
                handleException(e);
            }
        }

        @Override
        public void onHalfClose() {
            try {
                super.onHalfClose();
            } catch (Exception e) {
                handleException(e);
            }
        }

        @Override
        public void onCancel() {
            try {
                super.onCancel();
            } catch (Exception e) {
                handleException(e);
            }
        }

        @Override
        public void onComplete() {
            try {
                super.onComplete();
            } catch (Exception e) {
                handleException(e);
            }
        }

        @Override
        public void onReady() {
            try {
                super.onReady();
            } catch (Exception e) {
                handleException(e);
            }
        }

        private void handleException(Exception e) {
            log.error("Exception in gRPC call", e);

            Status status = mapExceptionToStatus(e);
            serverCall.close(status, metadata);
        }

        private Status mapExceptionToStatus(Exception e) {
            if (e instanceof BaseException) {
                BaseException baseException = (BaseException) e;
                return Status.fromCode(baseException.getGrpcStatusCode())
                        .withDescription(baseException.getUserMessage())
                        .withCause(e);
            }

            // Handle specific exception types
            if (e instanceof IllegalArgumentException) {
                return Status.INVALID_ARGUMENT
                        .withDescription("Invalid argument provided")
                        .withCause(e);
            }

            if (e instanceof IllegalStateException) {
                return Status.FAILED_PRECONDITION
                        .withDescription("Invalid state for operation")
                        .withCause(e);
            }

            if (e instanceof UnsupportedOperationException) {
                return Status.UNIMPLEMENTED
                        .withDescription("Operation not implemented")
                        .withCause(e);
            }

            // Default to internal error for unknown exceptions
            return Status.INTERNAL
                    .withDescription("Internal server error")
                    .withCause(e);
        }
    }
}
