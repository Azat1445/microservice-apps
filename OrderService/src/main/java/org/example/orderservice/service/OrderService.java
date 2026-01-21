package org.example.orderservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.orderservice.dto.request.CreateOrderRequestDto;
import org.example.orderservice.dto.request.OrderItemDto;
import org.example.orderservice.dto.response.OrderResponseDto;
import org.example.orderservice.entity.Order;
import org.example.orderservice.entity.enums.OrderStatus;
import org.example.orderservice.exception.InsufficientStockException;
import org.example.orderservice.exception.OrderNotFoundException;
import org.example.orderservice.exception.ProductNotFoundException;
import org.example.orderservice.grpc.InventoryGrpcClient;
import org.example.orderservice.kafka.OrderEventItem;
import org.example.orderservice.kafka.OrderEventProducer;
import org.example.orderservice.mapper.OrderMapperDto;
import org.example.orderservice.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapperDto orderMapper;
    private final InventoryGrpcClient inventoryClient;
    private final OrderEventProducer orderProducer;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public Page<OrderResponseDto> findAllOrdersByUser(Long userId, Pageable pageable) {
        log.info("Fetching orders for user {}", userId);

        Page<Order> orders = orderRepository.findByUserId(userId, pageable);

        return orders.map(orderMapper::toResponseDto);
    }

    @Transactional(readOnly = true)
    public OrderResponseDto findOrderById(Long orderId, Long userId) {
        log.info("Fetching order {} for user {}", orderId, userId);

        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));

        return orderMapper.toResponseDto(order);
    }

    @Transactional
    public OrderResponseDto createOrder(CreateOrderRequestDto request, Long userId) {
        log.info("Processing order for user {}", userId);

        // Проверяем наличие товаров, считаем стоимость и собираем обогащённые позиции для Kafka (price, sale, total_price)
        double totalPrice = 0;
        Map<Long, Integer> productQuantities = new HashMap<>();
        List<OrderEventItem> enrichedItems = new ArrayList<>();

        for (OrderItemDto item : request.getItems()) {
            log.debug("Checking product {} availability", item.getProductId());

            var productInfo = inventoryClient.checkProductAvailability(item.getProductId());

            if (!productInfo.getAvailable()) {
                throw new ProductNotFoundException("Product " + item.getProductId() + " not available");
            }

            if (productInfo.getQuantity() < item.getQuantity()) {
                throw new InsufficientStockException("Insufficient stock for product " + item.getProductId()
                        + ". Available: " + productInfo.getQuantity()
                        + ", requested: " + item.getQuantity());
            }

            double itemTotal = productInfo.getFinalPrice() * item.getQuantity();
            totalPrice += itemTotal;

            productQuantities.put(item.getProductId(), item.getQuantity());
            enrichedItems.add(OrderEventItem.builder()
                    .productId(item.getProductId())
                    .quantity(item.getQuantity())
                    .price(productInfo.getPrice())
                    .sale(productInfo.getSale())
                    .totalPrice(itemTotal)
                    .build());
        }

        // Резервация товара через gRPC
        for (Map.Entry<Long, Integer> entry : productQuantities.entrySet()) {
            log.debug("Reserving product {} quantity {}", entry.getKey(), entry.getValue());

            var reserveResult = inventoryClient.reserveProduct(entry.getKey(), entry.getValue());

            if (!reserveResult.getSuccess()) {
                log.error("Failed to reserve product {}: {}", entry.getKey(), reserveResult.getMessage());
                throw new InsufficientStockException("Failed to reserve product " + reserveResult.getMessage());
            }
        }

        Order order = orderMapper.toEntity(request);
        order.setUserId(userId);
        order.setTotalPrice(totalPrice);
        order.setStatus(OrderStatus.PENDING);
        order.setItemsJson(convertItemsToJson(request.getItems()));

        Order savedOrder = orderRepository.save(order);
        log.info("Order {} created with total price: {}", savedOrder.getId(), totalPrice);

        // Отправляем в Kafka (OrderEvent с items: product_id, quantity, price, sale, total_price для Notification Service)
        try {
            orderProducer.sendOrder(savedOrder, enrichedItems);
            savedOrder.setStatus(OrderStatus.SENT_TO_KAFKA);
            orderRepository.save(savedOrder);
            log.info("Order {} sent to Kafka", savedOrder.getId());
        } catch (Exception e) {
            log.error("Failed to send order {} to Kafka", savedOrder.getId(), e);
            savedOrder.setStatus(OrderStatus.FAILED);
            orderRepository.save(savedOrder);
        }

        return orderMapper.toResponseDto(savedOrder);
    }

    @Transactional
    public OrderResponseDto updateOrderStatus(Long orderId, Long userId, OrderStatus newStatus) {
        log.info("Updating order {} status to {} by user {}", orderId, newStatus, userId);

        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));

        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.FAILED) {
            throw new IllegalArgumentException("Cannot update completed or failed order");
        }

        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);

        log.info("Order {} status updated to {}", orderId, newStatus);

        return orderMapper.toResponseDto(updatedOrder);
    }

    @Transactional
    public void cancelOrder(Long orderId, Long userId) {
        log.info("Cancelling order {} by user {}", orderId, userId);

        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));

        // Отменить заказы можем только в статусе PENDING or SENT_TO_KAFKA
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.SENT_TO_KAFKA) {
            throw new IllegalStateException("Cannot cancel order in status: " + order.getStatus());
        }

        try {
            restoreOrderItems(order);
        } catch (Exception e) {
            log.error("Failed to restore items for order {}", orderId, e);
        }

        order.setStatus(OrderStatus.FAILED);
        orderRepository.save(order);

        log.info("Order {} cancelled successfully", orderId);
    }

    @Transactional
    public void deleteOrder(Long orderId, Long userId) {
        log.info("Deleting order {} by user {}", orderId, userId);

        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));

        // Удаляем только отмененные заказы
        if (order.getStatus() != OrderStatus.FAILED) {
            throw new IllegalArgumentException("Can only delete cancelled orders");
        }

        orderRepository.delete(order);

        log.info("Order {} deleted successfully", orderId);
    }

    private void restoreOrderItems(Order order) {
        try {
            OrderItemDto[] items = objectMapper.readValue(order.getItemsJson(), OrderItemDto[].class);

            for (OrderItemDto item : items) {
                log.debug("Restoring product {} quantity {}", item.getProductId(), item.getQuantity());
                inventoryClient.restoreProduct(item.getProductId(), item.getQuantity());
            }

            log.info("Successfully restored items for order {}", order.getId());
        } catch (JsonProcessingException e) {
            log.error("Failed to parse order items JSON" + e);
            throw new RuntimeException("Failed to restore order items" + e);
        }
    }

    private String convertItemsToJson(Object items) {
        try {
            return objectMapper.writeValueAsString(items);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert items to JSON" + e);
            throw new RuntimeException("Failed to serialize order items" + e);
        }
    }

}
