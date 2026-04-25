package com.retail.oa.repository;

/**
 * @program: retail-oa-backend
 * @description:
 * @author: MichaelLong
 * @create: 2026-03-14 22:33
 **/

import com.retail.oa.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.time.LocalDateTime;


/**
 * Repository for purchase order persistence and simple order queries.
 */
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByStatus(String status);
    List<Order> findAllByStatus(String status);
    long countByStatus(String status);

    List<Order> findBySupplierId(Long supplierId);
    List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
