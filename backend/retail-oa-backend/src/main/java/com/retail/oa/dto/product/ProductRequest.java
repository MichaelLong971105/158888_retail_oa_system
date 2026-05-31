package com.retail.oa.dto.product;

import com.retail.oa.entity.ProductStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request payload for product create and update operations.
 */
@Getter
@Setter
public class ProductRequest {

    @NotBlank(message = "Product name cannot be blank")
    @Size(max = 255, message = "Product name cannot exceed 255 characters")
    private String name;

    @NotBlank(message = "SKU cannot be blank")
    @Size(max = 50, message = "SKU cannot exceed 50 characters")
    private String sku;

    @Size(max = 64, message = "Barcode cannot exceed 64 characters")
    private String barcode;

    @Size(max = 50, message = "Category cannot exceed 50 characters")
    private String category;

    @Size(max = 100, message = "Brand cannot exceed 100 characters")
    private String brand;

    @Size(max = 100, message = "Specification cannot exceed 100 characters")
    private String specification;

    @NotBlank(message = "Unit cannot be blank")
    @Size(max = 20, message = "Unit cannot exceed 20 characters")
    private String unit;

    @NotNull(message = "Price cannot be null")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price cannot be less than 0")
    private BigDecimal price;

    @NotNull(message = "Stock cannot be null")
    @Min(value = 0, message = "Stock cannot be less than 0")
    private Integer stock;

    @NotNull(message = "Minimum stock cannot be null")
    @Min(value = 0, message = "Minimum stock cannot be less than 0")
    private Integer minStock;

    @NotNull(message = "Status cannot be null")
    private ProductStatus status;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;

    private List<Long> supplierIds;

    private Long supplierId;
}
