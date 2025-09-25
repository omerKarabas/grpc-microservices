package com.example.order.service;

import com.example.common.CommonProto.*;
import com.example.common.ResponseBuilder;
import com.example.common.constants.SpecialChars;
import com.example.common.util.StreamResponseHandler;
import com.example.order.OrderProto.*;
import com.example.order.constants.OrderErrorCode;
import com.example.order.entity.OrderEntity;
import com.example.order.entity.OrderItemEntity;
import com.example.order.entity.OrderStatus;
import com.example.order.mapper.OrderMapper;
import com.example.order.repository.OrderRepository;
import com.example.user.UserProto.*;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.example.common.util.CollectionUtil.sum;

@GrpcService
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl extends com.example.order.OrderServiceGrpc.OrderServiceImplBase {
    
    @GrpcClient("user-service")
    private com.example.user.UserServiceGrpc.UserServiceBlockingStub userServiceStub;
    
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Override
    public void createOrder(CreateOrderRequest orderCreationRequest, StreamObserver<CreateOrderResponse> responseObserver) {
        log.info("Creating order for customer: {}, Items: {}", orderCreationRequest.getUserId(), orderCreationRequest.getItemsCount());
        
        try {
            ValidateUserResponse customerValidation = validateCustomer(orderCreationRequest.getUserId());
            if (!customerValidation.getIsValid()) {
                StreamResponseHandler.respond(responseObserver, CreateOrderResponse.newBuilder()
                        .setResponse(ResponseBuilder.error(OrderErrorCode.INVALID_CUSTOMER.getMessage() + SpecialChars.COLON_SPACE.getValue() + customerValidation.getErrorMessage(), OrderErrorCode.INVALID_CUSTOMER.getCode()))
                        .build());
                return;
            }

            OrderEntity newOrder = OrderEntity.builder()
                    .customerId(orderCreationRequest.getUserId())
                    .totalPrice(calculateTotalPrice(orderCreationRequest))
                    .currentStatus(OrderStatus.PENDING)
                    .build();
            
            OrderEntity savedOrder = saveOrder(newOrder);
            
            List<OrderItemEntity> orderItems = orderMapper.mapToOrderItemEntities(
                    orderCreationRequest.getItemsList(), savedOrder);
            
            savedOrder.setOrderItems(orderItems);
            savedOrder = saveOrder(savedOrder);
            
            Order orderProto = orderMapper.toProto(savedOrder);
            
            StreamResponseHandler.respond(responseObserver, CreateOrderResponse.newBuilder()
                    .setResponse(ResponseBuilder.success("Order created successfully"))
                    .setOrder(orderProto)
                    .setUser(customerValidation.getUser())
                    .build());
                    
        } catch (Exception e) {
            log.error("Error creating order", e);
            StreamResponseHandler.respond(responseObserver, CreateOrderResponse.newBuilder()
                    .setResponse(ResponseBuilder.error(OrderErrorCode.ORDER_CREATE_ERROR.getMessage() + SpecialChars.COLON_SPACE.getValue() + e.getMessage(), OrderErrorCode.ORDER_CREATE_ERROR.getCode()))
                    .build());
        }
    }

    private double calculateTotalPrice(CreateOrderRequest orderCreationRequest) {
        return sum(orderCreationRequest.getItemsList(), item -> item.getPrice() * item.getQuantity());
    }

    @Override
    public void getOrder(GetOrderRequest request, StreamObserver<GetOrderResponse> responseObserver) {
        log.info("Get order: {}", request.getOrderId());
        
        try {
            Optional<OrderEntity> orderEntityOpt = findOrderById(request.getOrderId());
            
            if (orderEntityOpt.isEmpty()) {
                StreamResponseHandler.respond(responseObserver, GetOrderResponse.newBuilder()
                        .setResponse(ResponseBuilder.error(OrderErrorCode.ORDER_NOT_FOUND.getMessage(), OrderErrorCode.ORDER_NOT_FOUND.getCode()))
                        .build());
                return;
            }
            
            OrderEntity foundOrder = orderEntityOpt.get();
            GetUserResponse customerResponse = fetchCustomerDetails(foundOrder.getCustomerId());
            
            if (!customerResponse.getResponse().getSuccess()) {
                StreamResponseHandler.respond(responseObserver, GetOrderResponse.newBuilder()
                        .setResponse(ResponseBuilder.error(OrderErrorCode.CUSTOMER_NOT_FOUND.getMessage(), OrderErrorCode.CUSTOMER_NOT_FOUND.getCode()))
                        .build());
                return;
            }
            
            Order orderProto = orderMapper.toProto(foundOrder);
            
            StreamResponseHandler.respond(responseObserver, GetOrderResponse.newBuilder()
                    .setResponse(ResponseBuilder.success("Order found"))
                    .setOrder(orderProto)
                    .setUser(customerResponse.getUser())
                    .build());
                    
        } catch (Exception e) {
            log.error("Error getting order", e);
            StreamResponseHandler.respond(responseObserver, GetOrderResponse.newBuilder()
                    .setResponse(ResponseBuilder.error(OrderErrorCode.ORDER_FETCH_ERROR.getMessage() + SpecialChars.COLON_SPACE.getValue() + e.getMessage(), OrderErrorCode.ORDER_FETCH_ERROR.getCode()))
                    .build());
        }
    }

    @Override
    public void updateOrderStatus(UpdateOrderStatusRequest request, StreamObserver<UpdateOrderStatusResponse> responseObserver) {
        log.info("Update order status: ID={}, Status={}", request.getOrderId(), request.getStatus());
        
        try {
            Optional<OrderEntity> existingOrderOpt = findOrderById(request.getOrderId());
            
            if (existingOrderOpt.isEmpty()) {
                StreamResponseHandler.respond(responseObserver, UpdateOrderStatusResponse.newBuilder()
                        .setResponse(ResponseBuilder.error(OrderErrorCode.ORDER_TO_UPDATE_NOT_FOUND.getMessage(), OrderErrorCode.ORDER_TO_UPDATE_NOT_FOUND.getCode()))
                        .build());
                return;
            }
            
            OrderEntity existingOrder = existingOrderOpt.get();
            existingOrder.setCurrentStatus(orderMapper.mapToEntityOrderStatus(request.getStatus()));
            
            OrderEntity updatedOrder = saveOrder(existingOrder);
            Order orderProto = orderMapper.toProto(updatedOrder);
            
            StreamResponseHandler.respond(responseObserver, UpdateOrderStatusResponse.newBuilder()
                    .setResponse(ResponseBuilder.success("Order status updated successfully"))
                    .setOrder(orderProto)
                    .build());
                    
        } catch (Exception e) {
            log.error("Error updating order status", e);
            StreamResponseHandler.respond(responseObserver, UpdateOrderStatusResponse.newBuilder()
                    .setResponse(ResponseBuilder.error(OrderErrorCode.ORDER_UPDATE_ERROR.getMessage() + SpecialChars.COLON_SPACE.getValue() + e.getMessage(), OrderErrorCode.ORDER_UPDATE_ERROR.getCode()))
                    .build());
        }
    }

    @Override
    public void getUserOrders(GetUserOrdersRequest request, StreamObserver<GetUserOrdersResponse> responseObserver) {
        log.info("Get user orders: {}", request.getUserId());
        
        try {
            ValidateUserResponse customerValidation = validateCustomer(request.getUserId());
            if (!customerValidation.getIsValid()) {
                StreamResponseHandler.respond(responseObserver, GetUserOrdersResponse.newBuilder()
                        .setResponse(ResponseBuilder.error(OrderErrorCode.INVALID_CUSTOMER.getMessage() + SpecialChars.COLON_SPACE.getValue() + customerValidation.getErrorMessage(), OrderErrorCode.INVALID_CUSTOMER.getCode()))
                        .build());
                return;
            }
            
            List<OrderEntity> customerOrders = findOrdersByCustomerId(request.getUserId());
            List<Order> orderProtos = customerOrders.stream()
                    .map(orderMapper::toProto)
                    .toList();
            
            StreamResponseHandler.respond(responseObserver, GetUserOrdersResponse.newBuilder()
                    .setResponse(ResponseBuilder.success("Customer orders found"))
                    .addAllOrders(orderProtos)
                    .setUser(customerValidation.getUser())
                    .build());
                    
        } catch (Exception e) {
            log.error("Error fetching user orders", e);
            StreamResponseHandler.respond(responseObserver, GetUserOrdersResponse.newBuilder()
                    .setResponse(ResponseBuilder.error(OrderErrorCode.ORDER_FETCH_ERROR.getMessage() + SpecialChars.COLON_SPACE.getValue() + e.getMessage(), OrderErrorCode.ORDER_FETCH_ERROR.getCode()))
                    .build());
        }
    }

    @Override
    public void cancelOrder(CancelOrderRequest request, StreamObserver<CancelOrderResponse> responseObserver) {
        log.info("Cancel order: {}", request.getOrderId());
        
        try {
            Optional<OrderEntity> existingOrderOpt = findOrderById(request.getOrderId());
            
            if (existingOrderOpt.isEmpty()) {
                StreamResponseHandler.respond(responseObserver, CancelOrderResponse.newBuilder()
                        .setResponse(ResponseBuilder.error(OrderErrorCode.ORDER_TO_CANCEL_NOT_FOUND.getMessage(), OrderErrorCode.ORDER_TO_CANCEL_NOT_FOUND.getCode()))
                        .build());
                return;
            }
            
            OrderEntity existingOrder = existingOrderOpt.get();
            
            if (existingOrder.getCurrentStatus() == OrderStatus.DELIVERED) {
                StreamResponseHandler.respond(responseObserver, CancelOrderResponse.newBuilder()
                        .setResponse(ResponseBuilder.error(OrderErrorCode.ORDER_CANNOT_CANCEL.getMessage(), OrderErrorCode.ORDER_CANNOT_CANCEL.getCode()))
                        .build());
                return;
            }
            
            existingOrder.setCurrentStatus(OrderStatus.CANCELLED);
            OrderEntity cancelledOrder = saveOrder(existingOrder);
            Order orderProto = orderMapper.toProto(cancelledOrder);
            
            StreamResponseHandler.respond(responseObserver, CancelOrderResponse.newBuilder()
                    .setResponse(ResponseBuilder.success("Order cancelled successfully"))
                    .setOrder(orderProto)
                    .build());
                    
        } catch (Exception e) {
            log.error("Error cancelling order", e);
            StreamResponseHandler.respond(responseObserver, CancelOrderResponse.newBuilder()
                    .setResponse(ResponseBuilder.error(OrderErrorCode.ORDER_CANCEL_ERROR.getMessage() + SpecialChars.COLON_SPACE.getValue() + e.getMessage(), OrderErrorCode.ORDER_CANCEL_ERROR.getCode()))
                    .build());
        }
    }
    
    private ValidateUserResponse validateCustomer(long customerId) {
        ValidateUserRequest customerValidationRequest = ValidateUserRequest.newBuilder()
                .setUserId(customerId)
                .build();
        return userServiceStub.validateUser(customerValidationRequest);
    }
    
    private GetUserResponse fetchCustomerDetails(long customerId) {
        GetUserRequest customerDetailsRequest = GetUserRequest.newBuilder()
                .setUserId(customerId)
                .build();
        return userServiceStub.getUser(customerDetailsRequest);
    }
    
    private OrderEntity saveOrder(OrderEntity order) {
        log.debug("Saving order with ID: {}", order.getOrderId());
        return orderRepository.save(order);
    }
    
    // Helper methods for find operations
    private Optional<OrderEntity> findOrderById(Long orderId) {
        log.debug("Finding order by ID: {}", orderId);
        return orderRepository.findById(orderId);
    }
    
    private List<OrderEntity> findOrdersByCustomerId(Long customerId) {
        log.debug("Finding orders for customer ID: {}", customerId);
        return orderRepository.findByCustomerId(customerId);
    }
    
}