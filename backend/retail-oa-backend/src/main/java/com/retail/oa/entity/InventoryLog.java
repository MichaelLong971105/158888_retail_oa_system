package com.retail.oa.entity;

/**
 * @program: retail-oa-backend
 * @description:
 * @author: MichaelLong
 * @create: 2026-04-07 22:12
 **/

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Stores one inventory change record for a product.
 */
@Entity
@Getter
@Setter
@Table(name = "inventory_logs")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class InventoryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotBlank(message = "Change type cannot be blank")
    @Column(name = "change_type", nullable = false, length = 20)
    private String changeType;

    @Min(value = 1, message = "Quantity must be greater than 0")
    @Column(nullable = false)
    private Integer quantity;

    @Column(length = 255)
    private String remark;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public InventoryLog() {
    }

    public InventoryLog(Long id, Product product, String changeType, Integer quantity, String remark, LocalDateTime createdAt) {
        this.id = id;
        this.product = product;
        this.changeType = changeType;
        this.quantity = quantity;
        this.remark = remark;
        this.createdAt = createdAt;
    }

    /**
     * Automatically sets the creation timestamp before the record is inserted.
     */
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

}
