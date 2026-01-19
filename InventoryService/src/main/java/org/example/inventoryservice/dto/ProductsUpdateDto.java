package org.example.inventoryservice.dto;


import lombok.Data;

@Data
public class ProductsUpdateDto {

    private String name;
    private Long quantity;
    private Double price;
    private Long sale;
}
