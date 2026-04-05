package com.retail.oa.entity;

/**
 * @program: retail-oa-backend
 * @description: product entity
 * @author: MichaelLong
 * @create: 2026-03-14 22:30
 **/

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
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

    @DecimalMin(value = "0.0", inclusive = true, message = "Price cannot be less than 0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Min(value = 0, message = "Stock cannot be less than 0")
    @Column(nullable = false)
    private Integer stock;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    @Column(length = 255)
    private String description;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public Product() {
    }

    public Product(Long id, String name, String sku, String category, BigDecimal price,
                   Integer stock, String description, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.sku = sku;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.description = description;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public @NotBlank(message = "Product name cannot be blank") @Size(max = 255, message = "Product name cannot exceed 255 characters") String getName() {
        return name;
    }

    public void setName(@NotBlank(message = "Product name cannot be blank") @Size(max = 255, message = "Product name cannot exceed 255 characters") String name) {
        this.name = name;
    }

    public @NotBlank(message = "SKU cannot be blank") @Size(max = 50, message = "SKU cannot exceed 50 characters") String getSku() {
        return sku;
    }

    public void setSku(@NotBlank(message = "SKU cannot be blank") @Size(max = 50, message = "SKU cannot exceed 50 characters") String sku) {
        this.sku = sku;
    }

    public @Size(max = 50, message = "Category cannot exceed 50 characters") String getCategory() {
        return category;
    }

    public void setCategory(@Size(max = 50, message = "Category cannot exceed 50 characters") String category) {
        this.category = category;
    }

    public @DecimalMin(value = "0.0", inclusive = true, message = "Price cannot be less than 0") BigDecimal getPrice() {
        return price;
    }

    public void setPrice(@DecimalMin(value = "0.0", inclusive = true, message = "Price cannot be less than 0") BigDecimal price) {
        this.price = price;
    }

    public @Min(value = 0, message = "Stock cannot be less than 0") Integer getStock() {
        return stock;
    }

    public void setStock(@Min(value = 0, message = "Stock cannot be less than 0") Integer stock) {
        this.stock = stock;
    }

    public @Size(max = 255, message = "Description cannot exceed 255 characters") String getDescription() {
        return description;
    }

    public void setDescription(@Size(max = 255, message = "Description cannot exceed 255 characters") String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
