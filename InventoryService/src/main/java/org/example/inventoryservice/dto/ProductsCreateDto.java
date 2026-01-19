package org.example.inventoryservice.dto;

import lombok.Data;

@Data
public class ProductsCreateDto {

    private String name;
    private Long quantity;
    private Double price;
    private Long sale;
}

