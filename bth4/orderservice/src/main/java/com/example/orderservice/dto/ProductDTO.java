package com.example.orderservice.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductDTO {
    private Integer id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer quantity;
}