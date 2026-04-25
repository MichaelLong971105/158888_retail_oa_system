package com.retail.oa.dto.product;

import com.retail.oa.entity.ProductStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * API response for product data shown by the frontend.
 */
@Getter
@Setter
public class ProductResponse {

    private Long id;
    private String name;
    private String sku;
    private String barcode;
    private String category;
    private String brand;
    private String specification;
    private String unit;
    private BigDecimal price;
    private Integer stock;
    private Integer minStock;
    private ProductStatus status;
    private String description;
    private Long supplierId;
    private String supplierName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
