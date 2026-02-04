package org.example.inventoryservice.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProductsResponseDto {

    private Long id;
    private String name;
    private Long quantity;
    private Double price;
    private Long sale;
    private Double finalPrice;
    private Boolean available;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
