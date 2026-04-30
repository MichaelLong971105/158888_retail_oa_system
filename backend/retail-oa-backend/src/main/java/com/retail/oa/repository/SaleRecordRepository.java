package com.retail.oa.repository;

import com.retail.oa.entity.SaleRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for sales receipt persistence and summary queries.
 */
public interface SaleRecordRepository extends JpaRepository<SaleRecord, Long> {

    Optional<SaleRecord> findBySaleNumber(String saleNumber);

    List<SaleRecord> findAllByOrderBySaleTimeDesc();

    List<SaleRecord> findBySaleTimeBetweenOrderBySaleTimeDesc(LocalDateTime start, LocalDateTime end);

    List<SaleRecord> findByCashierIdOrderBySaleTimeDesc(Long cashierId);

    List<SaleRecord> findBySourceOrderBySaleTimeDesc(com.retail.oa.entity.SaleSource source);

    @Query("""
            select coalesce(sum(sr.totalAmount), 0)
            from SaleRecord sr
            where sr.saleTime between :start and :end
            """)
    BigDecimal sumTotalAmountBetween(LocalDateTime start, LocalDateTime end);

    long countBySaleTimeBetween(LocalDateTime start, LocalDateTime end);
}
