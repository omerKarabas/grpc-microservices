package com.example.order.mapper;

import com.example.common.CommonProto.*;
import com.example.common.util.CollectionUtil;
import com.example.order.entity.OrderEntity;
import com.example.order.entity.OrderItemEntity;
import com.example.order.entity.OrderStatus;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class OrderMapper {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public Order toProto(OrderEntity orderEntity) {
        if (orderEntity == null) return null;

        Order.Builder orderBuilder = Order.newBuilder()
                .setId(orderEntity.getOrderId())
                .setUserId(orderEntity.getCustomerId())
                .setTotalAmount(orderEntity.getTotalPrice())
                .setStatus(mapToProtoOrderStatus(orderEntity.getCurrentStatus()))
                .setCreatedAt(orderEntity.getCreatedAt().format(DATE_TIME_FORMATTER));

        if (CollectionUtil.isNotEmpty(orderEntity.getOrderItems())) {
            List<OrderItem> protoItems = orderEntity.getOrderItems().stream()
                    .map(this::mapToOrderItemProto)
                    .toList();
            orderBuilder.addAllItems(protoItems);
        }

        return orderBuilder.build();
    }

    public OrderItem mapToOrderItemProto(OrderItemEntity orderItemEntity) {
        if (orderItemEntity == null) return null;

        return OrderItem.newBuilder()
                .setProductId(orderItemEntity.getProductId())
                .setProductName(orderItemEntity.getProductTitle())
                .setQuantity(orderItemEntity.getItemQuantity())
                .setPrice(orderItemEntity.getUnitPrice())
                .build();
    }

    public List<OrderItemEntity> mapToOrderItemEntities(List<OrderItem> orderItemProtos, OrderEntity orderEntity) {
        if (CollectionUtil.isEmpty(orderItemProtos)) {
            return List.of();
        }

        return orderItemProtos.stream()
                .map(item -> OrderItemEntity.builder()
                        .productId(item.getProductId())
                        .productTitle(item.getProductName())
                        .itemQuantity(item.getQuantity())
                        .unitPrice(item.getPrice())
                        .order(orderEntity)
                        .build())
                .toList();
    }


    public com.example.common.CommonProto.OrderStatus mapToProtoOrderStatus(OrderStatus entityStatus) {
        if (entityStatus == null) return com.example.common.CommonProto.OrderStatus.UNKNOWN;

        return switch (entityStatus) {
            case UNKNOWN -> com.example.common.CommonProto.OrderStatus.UNKNOWN;
            case PENDING -> com.example.common.CommonProto.OrderStatus.PENDING;
            case CONFIRMED -> com.example.common.CommonProto.OrderStatus.CONFIRMED;
            case SHIPPED -> com.example.common.CommonProto.OrderStatus.SHIPPED;
            case DELIVERED -> com.example.common.CommonProto.OrderStatus.DELIVERED;
            case CANCELLED -> com.example.common.CommonProto.OrderStatus.CANCELLED;
        };
    }

    public OrderStatus mapToEntityOrderStatus(com.example.common.CommonProto.OrderStatus protoStatus) {
        if (protoStatus == null) return OrderStatus.UNKNOWN;

        return switch (protoStatus) {
            case UNKNOWN -> OrderStatus.UNKNOWN;
            case PENDING -> OrderStatus.PENDING;
            case CONFIRMED -> OrderStatus.CONFIRMED;
            case SHIPPED -> OrderStatus.SHIPPED;
            case DELIVERED -> OrderStatus.DELIVERED;
            case CANCELLED -> OrderStatus.CANCELLED;
            default -> OrderStatus.UNKNOWN;
        };
    }

    /**
     * Maps a list of OrderEntity to a list of Order proto objects
     * @param orderEntities List of OrderEntity objects
     * @return List of Order proto objects
     */
    public List<Order> mapToProtoList(List<OrderEntity> orderEntities) {
        if (CollectionUtil.isEmpty(orderEntities)) {
            return List.of();
        }

        return orderEntities.stream()
                .map(this::toProto)
                .toList();
    }
}
