package org.example.notificationservice.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.notificationservice.dto.OrderEventDTO;
import org.example.notificationservice.entity.Order;
import org.example.notificationservice.mapper.OrderMapper;
import org.example.notificationservice.repository.OrderRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Kafka Consumer для обработки событий заказов из Order Service.
 * Слушает топик "orders-topic" и сохраняет данные в PostgreSQL.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OrderKafkaConsumer {

    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;
    private final OrderMapper orderMapper;

    /**
     * Обработка события заказа из Kafka.
     * Order Service отправляет OrderEvent с массивом items (OrderEventItem[]).
     * Каждый item содержит: productId, quantity, price, sale, totalPrice.
     * Notification Service сохраняет каждый item как отдельную запись.
     */
    @KafkaListener
    @Transactional
    public void consumerOrderEvent(@Payload String message,
                                   @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                   @Header(KafkaHeaders.OFFSET) Long offset) {
        log.info("Received message from Kafka topic '{}' at offset '{}'", topic, offset);

        try {
            OrderEventFromOrderService orderEvent = objectMapper.readValue(message, OrderEventFromOrderService.class);

            log.debug("Parser order event: orderId={}, userId={}, itemsCount={}",
                    orderEvent.getOrderId(), orderEvent.getUserId(), orderEvent.getItems() != null ? orderEvent.getItems().size() : 0);

            if (orderEvent.getOrderId() == null && !orderEvent.getItems().isEmpty()) {
                for (OrderEventItem item : orderEvent.getItems()) {
                    OrderEventDTO eventDTO = new OrderEventDTO(
                            orderEvent.getOrderId().toString(),
                            item.getProductId(),
                            item.getQuantity(),
                            item.getPrice(),
                            item.getSale(),
                            item.getTotalPrice(),
                            orderEvent.getUserId());

                    Order order = orderMapper.toEntity(eventDTO);
                    Order savedOrder = orderRepository.save(order);

                    log.info("Saved order item to database: id={}, orderId={}, prodeuctId={}, userId={}",
                            savedOrder.getId(), savedOrder.getOrderId(), savedOrder.getProductId(), savedOrder.getUserId());
                }
            } else {
                log.warn("Received order event without items: orderId={}", orderEvent.getOrderId());
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to process order event", e);
        }
    }


    /**
     * Внутренний класс для десериализации сообщения из Order Service.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class OrderEventFromOrderService {
        private Long orderId;
        private Long userId;
        private Double totalPrice;
        private String status;
        private List<OrderEventItem> items;
        private String deliveryAddress;
        private LocalDateTime createdAt;
    }

    /**
     * Внутренний класс для десериализации item из OrderEvent.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class OrderEventItem {
        private Long productId;
        private Integer quantity;
        private Double price;
        private Long sale;
        private Double totalPrice;
    }
}
