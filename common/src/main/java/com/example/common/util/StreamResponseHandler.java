package com.example.common.util;

import io.grpc.stub.StreamObserver;
import lombok.experimental.UtilityClass;

/**
 * Handler for gRPC stream response operations
 * Provides common functionality for sending responses and completing streams
 */
@UtilityClass
public class StreamResponseHandler {

    /**
     * Send response and complete the stream
     * @param responseObserver The gRPC response observer
     * @param response The response to send
     * @param <T> The response type
     */
    public static <T> void respond(StreamObserver<T> responseObserver, T response) {
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
