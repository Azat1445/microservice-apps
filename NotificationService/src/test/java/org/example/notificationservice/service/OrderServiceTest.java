package org.example.notificationservice.service;

import org.example.notificationservice.dto.OrderResponseDTO;
import org.example.notificationservice.entity.Order;
import org.example.notificationservice.exception.OrderNotFoundException;
import org.example.notificationservice.exception.ResourceNotFoundException;
import org.example.notificationservice.mapper.OrderMapper;
import org.example.notificationservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    private Order testOrder;
    private OrderResponseDTO testOrderDTO;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        testOrder = Order.builder()
                .id(1L)
                .orderId("ORDER-123")
                .productId(100L)
                .quantity(2)
                .price(50.0)
                .sale(10L)
                .totalPrice(90.0)
                .userId(5L)
                .createdAt(LocalDateTime.now())
                .build();

        testOrderDTO = new OrderResponseDTO(
                1L,
                "ORDER-123",
                100L,
                2,
                50.0,
                10L,
                90.0,
                5L,
                LocalDateTime.now()
        );

        pageable = PageRequest.of(0, 20);
    }


    @Test
    void findAllOrdersSuccess() {
        Page<Order> orderPage = new PageImpl<>(List.of(testOrder));

        when(orderRepository.findAll(any(Pageable.class))).thenReturn(orderPage);

        when(orderMapper.toResponseDTO(any(Order.class))).thenReturn(testOrderDTO);

        Page<OrderResponseDTO> result = orderService.findAllOrders(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("ORDER-123", result.getContent().get(0).getOrderId());

        verify(orderRepository, times(1)).findAll(pageable);
        verify(orderMapper, times(1)).toResponseDTO(testOrder);
    }

    @Test
    void findOrderByOrderIdSuccess() {
        String orderId = "ORDER-123";
        Page<Order> orderPage = new PageImpl<>(List.of(testOrder));

        when(orderRepository.findOrderByOrderId(orderId, pageable)).thenReturn(orderPage);
        when(orderMapper.toResponseDTO(testOrder)).thenReturn(testOrderDTO);

        Page<OrderResponseDTO> result = orderService.findOrderByOrderId(orderId, pageable);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals("ORDER-123", result.getContent().get(0).getOrderId());

        verify(orderRepository).findOrderByOrderId(orderId, pageable);
    }

    @Test
    void findOrderByOrderIdNotFound() {
        String orderId = "ORDER-123";
        Page<Order> emtyPage = new PageImpl<>(List.of());

        when(orderRepository.findOrderByOrderId(orderId, pageable)).thenReturn(emtyPage);

        assertThrows(OrderNotFoundException.class, () -> orderService.findOrderByOrderId(orderId, pageable));

        verify(orderMapper, never()).toResponseDTO(any());
    }

    @Test
    void findOrderByUserIdSuccess() {
        Long userId = 5L;
        Page<Order> orderPage = new PageImpl<>(List.of(testOrder));

        when(orderRepository.findOrderByUserId(userId, pageable)).thenReturn(orderPage);
        when(orderMapper.toResponseDTO(testOrder)).thenReturn(testOrderDTO);

        Page<OrderResponseDTO> result = orderService.findOrderByUserId(userId, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(5L, result.getContent().get(0).getUserId());
    }

    @Test
    void findOrderByUserIdNotFound() {
        Long userId = 999L;
        Page<Order> orderPage = new PageImpl<>(List.of());

        when(orderRepository.findOrderByUserId(userId, pageable)).thenReturn(orderPage);

        assertThrows(ResourceNotFoundException.class, () -> orderService.findOrderByUserId(userId, pageable));
    }
}
