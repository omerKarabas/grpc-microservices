package com.example.order.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Error codes and messages for Order Service operations
 */
@Getter
@RequiredArgsConstructor
public enum OrderErrorCode {
    
    // Order not found errors
    ORDER_NOT_FOUND("ORDER_NOT_FOUND", "Order not found"),
    ORDER_TO_UPDATE_NOT_FOUND("ORDER_NOT_FOUND", "Order to update not found"),
    ORDER_TO_CANCEL_NOT_FOUND("ORDER_NOT_FOUND", "Order to cancel not found"),
    
    // Customer validation errors
    INVALID_CUSTOMER("INVALID_CUSTOMER", "Invalid customer"),
    CUSTOMER_NOT_FOUND("CUSTOMER_NOT_FOUND", "Customer not found for order"),
    
    // Order creation errors
    ORDER_CREATE_ERROR("ORDER_CREATE_ERROR", "Failed to create order"),
    
    // Order fetch errors
    ORDER_FETCH_ERROR("ORDER_FETCH_ERROR", "Failed to fetch order"),
    
    // Order update errors
    ORDER_UPDATE_ERROR("ORDER_UPDATE_ERROR", "Failed to update order status"),
    
    // Order cancellation errors
    ORDER_CANNOT_CANCEL("ORDER_CANNOT_CANCEL", "Cannot cancel delivered order"),
    ORDER_CANCEL_ERROR("ORDER_CANCEL_ERROR", "Failed to cancel order");
    
    private final String code;
    private final String message;
    
    @Override
    public String toString() {
        return code;
    }
}
