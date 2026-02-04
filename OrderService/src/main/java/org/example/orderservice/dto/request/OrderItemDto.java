package org.example.orderservice.dto.request;

import lombok.Data;

@Data
public class OrderItemDto {

    private Long productId;
    private Integer quantity;
}
