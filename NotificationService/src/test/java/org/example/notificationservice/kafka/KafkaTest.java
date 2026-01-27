package org.example.notificationservice.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.notificationservice.entity.Order;
import org.example.notificationservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext
@EmbeddedKafka(partitions = 1,
        topics = {"orders-topic"},
        bootstrapServersProperty = "spring.kafka.bootstrap-servers")
public class KafkaTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private OrderRepository orderRepository;


    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
    }

    @Test
    void consumeOrderEventSuccess() {
        String kafkaMessage = """
                   {
                  "orderId": 123,
                  "userId": 5,
                  "totalPrice": 150.0,
                  "status": "SENT_TO_KAFKA",
                  "items": [
                    {
                      "productId": 100,
                      "quantity": 2,
                      "price": 50.0,
                      "sale": 10,
                      "totalPrice": 90.0
                    }
                  ],
                  "deliveryAddress": "Test Street 123",
                  "createdAt": "2026-01-26T12:00:00"
                }
                """;

        kafkaTemplate.send("orders-topic", "ORDER-123", kafkaMessage);

        await()
                .atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    // Проверяем, что заказ сохранился в БД
                    List<Order> orders = orderRepository.findAll();
                    assertEquals(1, orders.size(), "Должен быть 1 заказ в БД");

                    Order savedOrder = orders.get(0);
                    assertEquals("123", savedOrder.getOrderId());
                    assertEquals(5L, savedOrder.getUserId());
                    assertEquals(100L, savedOrder.getProductId());
                    assertEquals(2, savedOrder.getQuantity());
                    assertEquals(50.0, savedOrder.getPrice());
                    assertEquals(10L, savedOrder.getSale());
                    assertEquals(90.0, savedOrder.getTotalPrice());
                });
    }

    @Test
    void consumeOrderEventMultipleItems() throws Exception {
        String kafkaMessage = """
                {
                  "orderId": 456,
                  "userId": 10,
                  "totalPrice": 300.0,
                  "status": "SENT_TO_KAFKA",
                  "items": [
                    {
                      "productId": 100,
                      "quantity": 2,
                      "price": 50.0,
                      "sale": 0,
                      "totalPrice": 100.0
                    },
                    {
                      "productId": 200,
                      "quantity": 1,
                      "price": 200.0,
                      "sale": 0,
                      "totalPrice": 200.0
                    }
                  ],
                  "deliveryAddress": "Test Street 456",
                  "createdAt": "2026-01-26T12:00:00"
                }
                """;

        kafkaTemplate.send("orders-topic", "ORDER-123", kafkaMessage);

        await()
                .atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    List<Order> orders = orderRepository.findAll();
                    assertEquals(2, orders.size(), "Должно быть 2 записи");

                    Order item1 = orders.stream()
                            .filter(o -> o.getProductId().equals(100L))
                            .findFirst()
                            .orElseThrow();
                    assertEquals("456", item1.getOrderId());
                    assertEquals(100.0, item1.getTotalPrice());

                    Order item2 = orders.stream()
                            .filter(o -> o.getProductId().equals(200L))
                            .findFirst()
                            .orElseThrow();
                    assertEquals("456", item2.getOrderId());
                    assertEquals(200.0, item2.getTotalPrice());
                });
    }
}
