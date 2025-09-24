package com.example.user.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Error codes and messages for User Service operations
 */
@Getter
@RequiredArgsConstructor
public enum UserErrorCode {
    
    // User not found errors
    USER_NOT_FOUND("USER_NOT_FOUND", "User not found"),
    USER_TO_UPDATE_NOT_FOUND("USER_NOT_FOUND", "User to update not found"),
    USER_TO_DELETE_NOT_FOUND("USER_NOT_FOUND", "User to delete not found"),
    
    // User creation errors
    USER_ALREADY_EXISTS("USER_ALREADY_EXISTS", "User with email already exists"),
    USER_CREATE_ERROR("USER_CREATE_ERROR", "Failed to create user"),
    
    // User fetch errors
    USER_FETCH_ERROR("USER_FETCH_ERROR", "Failed to fetch user"),
    
    // User update errors
    USER_UPDATE_ERROR("USER_UPDATE_ERROR", "Failed to update user"),
    
    // User delete errors
    USER_DELETE_ERROR("USER_DELETE_ERROR", "Failed to delete user");
    
    private final String code;
    private final String message;
    
    @Override
    public String toString() {
        return code;
    }
}
