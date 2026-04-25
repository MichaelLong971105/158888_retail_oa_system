package com.retail.oa.entity;

/**
 * @program: retail-oa-backend
 * @description: product entity
 * @author: MichaelLong
 * @create: 2026-03-14 22:30
 **/

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a managed product in the internal supermarket system.
 */
@Entity
@Getter
@Setter
@Table(name = "products")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Product name cannot be blank")
    @Size(max = 255, message = "Product name cannot exceed 255 characters")
    @Column(nullable = false, length = 255)
    private String name;

    @NotBlank(message = "SKU cannot be blank")
    @Size(max = 50, message = "SKU cannot exceed 50 characters")
    @Column(nullable = false, unique = true, length = 50)
    private String sku;

    @Size(max = 50, message = "Category cannot exceed 50 characters")
    @Column(length = 50)
    private String category;

    @Size(max = 64, message = "Barcode cannot exceed 64 characters")
    @Column(unique = true, length = 64)
    private String barcode;

    @Size(max = 100, message = "Brand cannot exceed 100 characters")
    @Column(length = 100)
    private String brand;

    @Size(max = 100, message = "Specification cannot exceed 100 characters")
    @Column(length = 100)
    private String specification;

    @NotBlank(message = "Unit cannot be blank")
    @Size(max = 20, message = "Unit cannot exceed 20 characters")
    @Column(nullable = false, length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'pcs'")
    private String unit;

    @DecimalMin(value = "0.0", inclusive = true, message = "Price cannot be less than 0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Min(value = 0, message = "Stock cannot be less than 0")
    @Column(nullable = false)
    private Integer stock;

    @NotNull(message = "Minimum stock cannot be null")
    @Min(value = 0, message = "Minimum stock cannot be less than 0")
    @Column(name = "min_stock", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer minStock;

    @NotNull(message = "Status cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'ACTIVE'")
    private ProductStatus status;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    @Column(length = 255)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Supplier supplier;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    public Product() {
    }

    public Product(Long id, String name, String sku, String category, String barcode, String brand,
                   String specification, String unit, BigDecimal price, Integer stock, Integer minStock,
                   ProductStatus status, String description, Supplier supplier, LocalDateTime createdAt,
                   LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.sku = sku;
        this.category = category;
        this.barcode = barcode;
        this.brand = brand;
        this.specification = specification;
        this.unit = unit;
        this.price = price;
        this.stock = stock;
        this.minStock = minStock;
        this.status = status;
        this.description = description;
        this.supplier = supplier;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
