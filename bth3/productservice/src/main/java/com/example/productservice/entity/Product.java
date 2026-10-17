package com.example.productservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Data
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // INT (PRIMARY KEY)

    @Column(nullable = false, length = 255)
    private String name; // VARCHAR(255)

    @Column(columnDefinition = "TEXT")
    private String description; // TEXT

    @Column(precision = 10, scale = 2)
    private BigDecimal price; // DECIMAL(10, 2)

    private Integer quantity; // INT

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // TIMESTAMP

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // TIMESTAMP
}