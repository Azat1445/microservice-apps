package org.example.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * DTO для получения события из Kafka (от Order Service).
 */
@Data
@AllArgsConstructor
@Builder
public class OrderEventDTO {

    private String orderId;
    private Long productId;
    private Integer quantity;
    private Double price;
    private Long sale;
    private Double totalPrice;
    private Long userId;
}
