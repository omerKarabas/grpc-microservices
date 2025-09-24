package com.example.order.entity;

import com.example.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true, exclude = "order")
public class OrderItemEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderItemId;

    @Column(nullable = false, name = "product_id")
    private Long productId;

    @Column(nullable = false, length = 200, name = "product_name")
    private String productTitle;

    @Column(nullable = false, name = "item_quantity")
    private Integer itemQuantity;

    @Column(nullable = false, precision = 10, scale = 2, name = "unit_price")
    private Double unitPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

}
