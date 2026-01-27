package org.example.notificationservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.notificationservice.dto.OrderResponseDTO;
import org.example.notificationservice.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/orders")
@Slf4j
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * GET /api/orders/all?page=0&size=20
     * Получить все заказы из таблицы orders с пагинацией.
     *
     * @param pageable параметры пагинации (page, size, sort)
     * @return Page с заказами
     */
    @GetMapping("/all")
    public ResponseEntity<Page<OrderResponseDTO>> findAllOrders(@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Fetching all orders");

        Page<OrderResponseDTO> orders = orderService.findAllOrders(pageable);

        log.info("Found {} orders (page {} of {})",
                orders.getNumberOfElements(),
                orders.getNumber(),
                orders.getTotalElements());
        return ResponseEntity.ok(orders);
    }

    /**
     * GET /api/orders/{order_id}
     * Получить все покупки по одному заказу.
     * Один заказ может содержать несколько товаров (productId).
     *
     * @param orderId идентификатор заказа (order_id)
     * @param pageable параметры пагинации
     * @return Page с товарами в заказе
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<Page<OrderResponseDTO>> findOrderByOrderId(@PathVariable String orderId,
                                                          @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET /api/orders/{} - page: {}, size: {}", orderId, pageable.getPageNumber(), pageable.getPageSize());

        Page<OrderResponseDTO> orders = orderService.findOrderByOrderId(orderId, pageable);

        log.info("Found {} items for orderId '{}' (page {} of {})",
                orders.getNumberOfElements(),
                orderId,
                orders.getNumber(),
                orders.getTotalPages());

        return ResponseEntity.ok(orders);
    }

    /**
     * GET /api/orders/user/{user_id}
     * Получить все покупки по одному пользователю.
     * Показывает полную историю покупок пользователя.
     *
     * @param userId идентификатор пользователя (user_id)
     * @param pageable параметры пагинации
     * @return Page с покупками пользователя
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<OrderResponseDTO>> findOrderByUserId(@PathVariable Long userId,
                                                                    @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable){
        log.info("GET /api/orders/user/{} - page: {}, size: {}", userId, pageable.getPageNumber(), pageable.getPageSize());

        Page<OrderResponseDTO> orders = orderService.findOrderByUserId(userId, pageable);

        log.info("Found {} orders for userId {} (page {} of {})",
                orders.getNumberOfElements(),
                userId,
                orders.getNumber(),
                orders.getTotalPages());

        return ResponseEntity.ok(orders);
    }
}
