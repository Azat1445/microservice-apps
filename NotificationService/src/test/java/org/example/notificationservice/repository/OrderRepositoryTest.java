package org.example.notificationservice.repository;

import org.example.notificationservice.entity.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@ActiveProfiles("test")
public class OrderRepositoryTest {

    private OrderRepository orderRepository;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();

        testOrder = Order.builder()
                .orderId("ORDER-123")
                .productId(100L)
                .quantity(2)
                .price(50.0)
                .sale(10L)
                .totalPrice(90.0)
                .userId(5L)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void saveOrderSuccess() {
        Order savedOrder = orderRepository.save(testOrder);

        assertNotNull(savedOrder.getId());
        assertEquals("ORDER-123", savedOrder.getOrderId());
    }

    @Test
    void findOrderByOrderIdSuccess() {
        orderRepository.save(testOrder);
        Pageable pageable = PageRequest.of(0, 10);

        Page<Order> result = orderRepository.findOrderByOrderId("ORDER-123", pageable);

        assertFalse(result.isEmpty());
        assertEquals("ORDER-123", result.getContent().get(0).getOrderId());
    }

    @Test
    void findOrderByUserIdSuccess() {
        orderRepository.save(testOrder);
        Pageable pageable = PageRequest.of(0, 10);

        Page<Order> result = orderRepository.findOrderByUserId(5L, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(5L, result.getContent().get(0).getUserId());
    }

    @Test
    void findOrderByOrderIdNotFound() {
        Page<Order> result = orderRepository.findOrderByOrderId("ORDER-123", PageRequest.of(0, 10));

        assertTrue(result.isEmpty());
    }

}
