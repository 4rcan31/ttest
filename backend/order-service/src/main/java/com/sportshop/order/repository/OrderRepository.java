package com.sportshop.order.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.sportshop.order.domain.Order;
import com.sportshop.order.domain.OrderStatus;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByUserId(Long userId, Pageable pageable);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    Optional<Order> findByOrderNumber(String orderNumber);

    /** Búsqueda acotada al dueño: una orden ajena responde 404, sin revelar que existe (evita IDOR). */
    Optional<Order> findByOrderNumberAndUserId(String orderNumber, Long userId);

    boolean existsByOrderNumber(String orderNumber);
}
