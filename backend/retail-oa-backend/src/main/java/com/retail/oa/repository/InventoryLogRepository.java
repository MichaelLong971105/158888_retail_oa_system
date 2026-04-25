package com.retail.oa.repository;

/**
 * @program: retail-oa-backend
 * @description:
 * @author: MichaelLong
 * @create: 2026-04-07 22:12
 **/

import com.retail.oa.entity.InventoryLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for inventory log persistence and product-based queries.
 */
public interface InventoryLogRepository extends JpaRepository<InventoryLog, Long> {

    List<InventoryLog> findByProductIdOrderByCreatedAtDesc(Long productId);
}
