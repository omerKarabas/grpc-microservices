package com.example.order.entity;

import com.example.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true, exclude = "orderItems")
public class OrderEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @Column(nullable = false, name = "customer_id")
    private Long customerId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItemEntity> orderItems;

    @Column(nullable = false, precision = 10, scale = 2, name = "total_price")
    private Double totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "order_status")
    private OrderStatus currentStatus;
}
