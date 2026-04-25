package com.retail.oa.repository;

/**
 * @program: retail-oa-backend
 * @description:
 * @author: MichaelLong
 * @create: 2026-03-14 22:33
 **/

import com.retail.oa.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for order item persistence.
 */
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
