package com.retail.oa.repository;

import com.retail.oa.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for order item persistence.
 */
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    boolean existsByProductId(Long productId);
}
