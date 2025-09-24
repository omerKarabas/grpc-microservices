package com.example.common;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ResponseBuilder {
    
    public static CommonProto.ApiResponse success(String message) {
        return CommonProto.ApiResponse.newBuilder()
                .setSuccess(true)
                .setMessage(message)
                .build();
    }
    
    public static CommonProto.ApiResponse error(String message, String errorCode) {
        return CommonProto.ApiResponse.newBuilder()
                .setSuccess(false)
                .setMessage(message)
                .setErrorCode(errorCode)
                .build();
    }
}
