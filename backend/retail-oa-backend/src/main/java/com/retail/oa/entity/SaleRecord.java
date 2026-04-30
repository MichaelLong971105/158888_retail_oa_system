package com.retail.oa.entity;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents one sales receipt imported from POS or created for testing.
 */
@Entity
@Getter
@Setter
@Table(name = "sale_records")
public class SaleRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sale_number", nullable = false, unique = true, length = 50)
    private String saleNumber;

    @Column(name = "external_sale_id", length = 100)
    private String externalSaleId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SaleSource source;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cashier_id")
    private User cashier;

    @Column(name = "cashier_name", nullable = false, length = 100)
    private String cashierName;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "sale_time", nullable = false)
    private LocalDateTime saleTime;

    @Column(length = 255)
    private String remark;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "saleRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SaleItem> items = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
