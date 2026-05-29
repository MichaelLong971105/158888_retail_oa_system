package com.retail.oa.repository;

import com.retail.oa.entity.InventoryLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for inventory log persistence and product-based queries.
 */
public interface InventoryLogRepository extends JpaRepository<InventoryLog, Long> {

    List<InventoryLog> findByProductIdOrderByCreatedAtDesc(Long productId);

    boolean existsByProductId(Long productId);
}
