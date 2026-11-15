package com.example.orderservice.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Data
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonBackReference
    private Order order;

    // Cho phép nhận "orderId" trong JSON khi gọi POST /order_items
    @Transient
    @JsonProperty("orderId")
    private Integer orderIdInput;

    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @Column(name = "product_name", length = 255)
    private String productName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_price", precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @JsonProperty("orderId")
    public Integer getOrderId() {
        return order != null ? order.getId() : orderIdInput;
    }
}