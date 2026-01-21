package org.example.orderservice.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Элемент заказа для Kafka-события.
 * Notification Service сохраняет в orders: order_id, product_id, quantity, price, sale, total_price, user_id.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEventItem {

    private Long productId;
    private Integer quantity;
    private Double price;
    private Long sale;
    private Double totalPrice;
}
