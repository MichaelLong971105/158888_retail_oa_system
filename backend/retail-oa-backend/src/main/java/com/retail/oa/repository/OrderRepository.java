package com.retail.oa.repository;

/**
 * @program: retail-oa-backend
 * @description:
 * @author: MichaelLong
 * @create: 2026-03-14 22:33
 **/

import com.retail.oa.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
