package com.retail.oa.repository;

import com.retail.oa.dto.sale.TopSellingProductResponse;
import com.retail.oa.entity.SaleItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for sales line items and product sales analytics.
 */
public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {

    @Query("""
            select new com.retail.oa.dto.sale.TopSellingProductResponse(
                si.product.id,
                si.productName,
                si.productSku,
                sum(si.quantity),
                sum(si.lineAmount)
            )
            from SaleItem si
            join si.saleRecord sr
            where sr.saleTime between :start and :end
            group by si.product.id, si.productName, si.productSku
            order by sum(si.quantity) desc, sum(si.lineAmount) desc
            """)
    List<TopSellingProductResponse> findTopSellingProductsBetween(
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );
}
