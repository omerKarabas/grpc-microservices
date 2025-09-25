package com.example.order.service;

import com.example.common.CommonProto.*;
import com.example.common.ResponseBuilder;
import com.example.common.exception.BusinessException;
import com.example.common.exception.ResourceNotFoundException;
import com.example.order.OrderProto.*;
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

        // Validate customer
        ValidateUserResponse customerValidation = validateCustomer(orderCreationRequest.getUserId());
        if (!customerValidation.getIsValid()) {
            throw new BusinessException(
                "INVALID_CUSTOMER",
                "Customer validation failed: " + customerValidation.getErrorMessage(),
                String.format("Customer validation failed for user ID '%s': %s", orderCreationRequest.getUserId(), customerValidation.getErrorMessage())
            );
        }

        // Create and save order
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

        CreateOrderResponse response = CreateOrderResponse.newBuilder()
                .setResponse(ResponseBuilder.success("Order created successfully"))
                .setOrder(orderProto)
                .setUser(customerValidation.getUser())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private double calculateTotalPrice(CreateOrderRequest orderCreationRequest) {
        return sum(orderCreationRequest.getItemsList(), item -> item.getPrice() * item.getQuantity());
    }

    @Override
    public void getOrder(GetOrderRequest request, StreamObserver<GetOrderResponse> responseObserver) {
        log.info("Get order: {}", request.getOrderId());

        Optional<OrderEntity> orderEntityOpt = findOrderById(request.getOrderId());

        if (orderEntityOpt.isEmpty()) {
            throw new ResourceNotFoundException(
                "ORDER_NOT_FOUND",
                "Order not found",
                String.format("Order with ID '%s' not found", request.getOrderId())
            );
        }

        OrderEntity foundOrder = orderEntityOpt.get();
        GetUserResponse customerResponse = fetchCustomerDetails(foundOrder.getCustomerId());

        if (!customerResponse.getResponse().getSuccess()) {
            throw new BusinessException(
                "CUSTOMER_NOT_FOUND",
                "Customer not found for order",
                String.format("Customer with ID '%s' not found for order '%s'", foundOrder.getCustomerId(), request.getOrderId())
            );
        }

        Order orderProto = orderMapper.toProto(foundOrder);

        GetOrderResponse response = GetOrderResponse.newBuilder()
                .setResponse(ResponseBuilder.success("Order found"))
                .setOrder(orderProto)
                .setUser(customerResponse.getUser())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void updateOrderStatus(UpdateOrderStatusRequest request, StreamObserver<UpdateOrderStatusResponse> responseObserver) {
        log.info("Update order status: ID={}, Status={}", request.getOrderId(), request.getStatus());

        Optional<OrderEntity> existingOrderOpt = findOrderById(request.getOrderId());

        if (existingOrderOpt.isEmpty()) {
            throw new ResourceNotFoundException(
                "ORDER_NOT_FOUND",
                "Order to update not found",
                String.format("Order with ID '%s' not found", request.getOrderId())
            );
        }

        OrderEntity existingOrder = existingOrderOpt.get();
        existingOrder.setCurrentStatus(orderMapper.mapToEntityOrderStatus(request.getStatus()));

        OrderEntity updatedOrder = saveOrder(existingOrder);
        Order orderProto = orderMapper.toProto(updatedOrder);

        UpdateOrderStatusResponse response = UpdateOrderStatusResponse.newBuilder()
                .setResponse(ResponseBuilder.success("Order status updated successfully"))
                .setOrder(orderProto)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getUserOrders(GetUserOrdersRequest request, StreamObserver<GetUserOrdersResponse> responseObserver) {
        log.info("Get user orders: {}", request.getUserId());

        ValidateUserResponse customerValidation = validateCustomer(request.getUserId());
        if (!customerValidation.getIsValid()) {
            throw new BusinessException(
                "INVALID_CUSTOMER",
                "Customer validation failed: " + customerValidation.getErrorMessage(),
                String.format("Customer validation failed for user ID '%s': %s", request.getUserId(), customerValidation.getErrorMessage())
            );
        }

        List<OrderEntity> customerOrders = findOrdersByCustomerId(request.getUserId());
        List<Order> orderProtos = customerOrders.stream()
                .map(orderMapper::toProto)
                .toList();

        GetUserOrdersResponse response = GetUserOrdersResponse.newBuilder()
                .setResponse(ResponseBuilder.success("Customer orders found"))
                .addAllOrders(orderProtos)
                .setUser(customerValidation.getUser())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void cancelOrder(CancelOrderRequest request, StreamObserver<CancelOrderResponse> responseObserver) {
        log.info("Cancel order: {}", request.getOrderId());

        Optional<OrderEntity> existingOrderOpt = findOrderById(request.getOrderId());

        if (existingOrderOpt.isEmpty()) {
            throw new ResourceNotFoundException(
                "ORDER_NOT_FOUND",
                "Order to cancel not found",
                String.format("Order with ID '%s' not found", request.getOrderId())
            );
        }

        OrderEntity existingOrder = existingOrderOpt.get();

        if (existingOrder.getCurrentStatus() == OrderStatus.DELIVERED) {
            throw new BusinessException(
                "ORDER_CANNOT_CANCEL",
                "Cannot cancel delivered order",
                String.format("Cannot cancel order '%s' with status DELIVERED", request.getOrderId())
            );
        }

        existingOrder.setCurrentStatus(OrderStatus.CANCELLED);
        OrderEntity cancelledOrder = saveOrder(existingOrder);
        Order orderProto = orderMapper.toProto(cancelledOrder);

        CancelOrderResponse response = CancelOrderResponse.newBuilder()
                .setResponse(ResponseBuilder.success("Order cancelled successfully"))
                .setOrder(orderProto)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
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