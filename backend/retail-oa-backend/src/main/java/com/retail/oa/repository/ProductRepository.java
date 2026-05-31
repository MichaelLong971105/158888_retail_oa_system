package com.retail.oa.repository;

import com.retail.oa.entity.Product;
import com.retail.oa.entity.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for product persistence and SKU-based lookups.
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    Optional<Product> findByBarcode(String barcode);

    boolean existsByBarcode(String barcode);

    List<Product> findByStatus(ProductStatus status);

    List<Product> findByStatusAndStockGreaterThan(ProductStatus status, Integer stock);

    boolean existsBySuppliers_Id(Long supplierId);
}
