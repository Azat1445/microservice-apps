package org.example.notificationservice.repository;

import org.example.notificationservice.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findOrderByOrderId(String orderId, Pageable pageable);

    Page<Order> findOrderByUserId(Long userId, Pageable pageable);
}
