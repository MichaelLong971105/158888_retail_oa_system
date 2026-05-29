package com.retail.oa.repository;

import com.retail.oa.entity.Order;
import com.retail.oa.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.time.LocalDateTime;


/**
 * Repository for purchase order persistence and simple order queries.
 */
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByStatus(OrderStatus status);
    List<Order> findByStatusIn(List<OrderStatus> statuses);
    List<Order> findAllByStatus(OrderStatus status);
    long countByStatus(OrderStatus status);
    long countByStatusIn(List<OrderStatus> statuses);

    List<Order> findBySupplierId(Long supplierId);
    boolean existsBySupplierId(Long supplierId);
    List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
