package org.example.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO для возврата данных через REST API.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponseDTO {

    private Long id;
    private String orderId;
    private Long productId;
    private Integer quantity;
    private Double price;
    private Long sale;
    private Double totalPrice;
    private Long userId;
    private LocalDateTime createdAt;
}
