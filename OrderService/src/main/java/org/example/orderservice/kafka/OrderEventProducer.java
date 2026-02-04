package org.example.orderservice.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.orderservice.entity.Order;
import org.example.orderservice.exception.KafkaProducerException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.orders:orders-topic}")
    private String ordersTopic;

    /**
     * Отправляет заказ в Kafka. items должны содержать price, sale, totalPrice для каждой позиции
     * (Notification Service сохраняет в orders: order_id, product_id, quantity, price, sale, total_price, user_id).
     */
    public void sendOrder(Order order, List<OrderEventItem> items) {
        log.info("Sending order {} to Kafka topic: {}", order.getId(), ordersTopic);

        try {
            OrderEvent event = OrderEvent.builder()
                    .orderId(order.getId())
                    .userId(order.getUserId())
                    .totalPrice(order.getTotalPrice())
                    .status(order.getStatus().name())
                    .items(items)
                    .deliveryAddress(order.getDeliveryAddress())
                    .createdAt(order.getCreatedAt())
                    .build();

            // Отправляем асинхронно
            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(ordersTopic, order.getId().toString(), event);

            // Обрабатываем результат (исключение в whenComplete не пробрасывается в вызывающий код)
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Order {} sent successfully to Kafka. Offset: {}",
                            order.getId(), result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to send order {} to Kafka", order.getId(), ex);
                }
            });

        } catch (Exception e) {
            log.error("Error sending order {} to Kafka", order.getId(), e);
            throw new KafkaProducerException("Kafka producer error", e);
        }
    }


    @Data
    @RequiredArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderEvent {
        private Long orderId;
        private Long userId;
        private Double totalPrice;
        private String status;
        /** Позиции заказа: product_id, quantity, price, sale, total_price — для Notification Service. */
        private List<OrderEventItem> items;
        private String deliveryAddress;
        private java.time.LocalDateTime createdAt;
    }
}
