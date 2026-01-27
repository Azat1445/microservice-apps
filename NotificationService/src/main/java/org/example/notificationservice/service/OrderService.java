package org.example.notificationservice.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.notificationservice.dto.OrderResponseDTO;
import org.example.notificationservice.entity.Order;
import org.example.notificationservice.exception.OrderNotFoundException;
import org.example.notificationservice.exception.ResourceNotFoundException;
import org.example.notificationservice.mapper.OrderMapper;
import org.example.notificationservice.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {

    public final OrderRepository orderRepository;
    public final OrderMapper orderMapper;

    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> findAllOrders(Pageable pageable) {
        log.info("Fetching all orders with pagination: {}", pageable);

        Page<Order> orders = orderRepository.findAll(pageable);

        log.debug("Found {} orders", orders.getTotalElements());
        return orders.map(orderMapper::toResponseDTO);
    }


    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> findOrderByOrderId(String orderId, Pageable pageable) {
        log.info("Fetching orders with order id {}", orderId);

        Page<Order> orders = orderRepository.findOrderByOrderId(orderId, pageable);

        if (orders.isEmpty()) {
            log.error("No orders found with order id {}", orderId);
            throw new OrderNotFoundException("Orders not found for order id " + orderId);
        }

        log.debug("Found {} items for order id '{}'", orders.getTotalElements(), orderId);
        return orders.map(orderMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> findOrderByUserId(Long userId, Pageable pageable) {
        log.info("Fetching orders by userId: {}", userId);

        Page<Order> orders = orderRepository.findOrderByUserId(userId, pageable);

        if (orders.isEmpty()) {
            log.error("No orders found for user id: {}", userId);
            throw new ResourceNotFoundException("No orders found for userId: " + userId);
        }

        log.debug("Found {} orders for userId {}", orders.getTotalElements(), userId);
        return orders.map(orderMapper::toResponseDTO);
    }
}
