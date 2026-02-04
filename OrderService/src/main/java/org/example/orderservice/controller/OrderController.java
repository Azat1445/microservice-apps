package org.example.orderservice.controller;

import org.springframework.data.domain.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.orderservice.dto.request.CreateOrderRequestDto;
import org.example.orderservice.dto.response.OrderResponseDto;
import org.example.orderservice.entity.enums.OrderStatus;
import org.example.orderservice.security.CustomUserDetails;
import org.example.orderservice.service.OrderService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping({"/api/orders", "/api/order"})
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<Page<OrderResponseDto>> findAllOrdersByUser(@AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("Fetching orders for user: {}", userDetails.getId());

        Page<OrderResponseDto> orders = orderService.findAllOrdersByUser(userDetails.getId(), pageable);

        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDto> findOrderById(@PathVariable Long id,
                                                          @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("Fetching order {} for user {}", id, userDetails.getId());

        OrderResponseDto order = orderService.findOrderById(id, userDetails.getId());

        return ResponseEntity.ok(order);
    }

    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(@Valid @RequestBody CreateOrderRequestDto createOrder,
                                                    @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("Creating order for user: {}", userDetails.getUsername());

        OrderResponseDto response = orderService.createOrder(createOrder, userDetails.getId());

        log.info("Order {} created successfully", response.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponseDto> updateOrderStatus(@PathVariable Long id,
                                                              @RequestParam OrderStatus status,
                                                              @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("Updating order {} status to {} by user {}", id, status, userDetails.getId());

        OrderResponseDto orders = orderService.updateOrderStatus(id, userDetails.getId(), status);

        return ResponseEntity.ok(orders);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long id,
                                            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("Cancelling order {} by user {}", id, userDetails.getId());

        orderService.cancelOrder(id, userDetails.getId());

        log.info("Order {} cancelled successfully", id);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id,
                                            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("Deleting order {} by user {}", id, userDetails.getId());

        orderService.deleteOrder(id, userDetails.getId());

        log.info("Order {} deleted successfully", id);

        return ResponseEntity.noContent().build();
    }
}
