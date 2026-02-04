package org.example.orderservice.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderResponseDto {

    private Long id;
    private Long userId;
    private Double totalPrice;
    private String status;
    private String itemsJson;
    private String deliveryAddress;
    private LocalDateTime createdAt;
}
