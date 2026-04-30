package com.retail.oa.dto.sale;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Aggregate response for top-selling products.
 */
@Getter
@Setter
public class TopSellingProductResponse {

    private Long productId;
    private String productName;
    private String productSku;
    private Long totalQuantity;
    private BigDecimal totalAmount;

    public TopSellingProductResponse(Long productId, String productName, String productSku,
                                     Long totalQuantity, BigDecimal totalAmount) {
        this.productId = productId;
        this.productName = productName;
        this.productSku = productSku;
        this.totalQuantity = totalQuantity;
        this.totalAmount = totalAmount;
    }
}
