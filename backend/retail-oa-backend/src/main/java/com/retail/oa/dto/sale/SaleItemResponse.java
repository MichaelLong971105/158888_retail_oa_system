package com.retail.oa.dto.sale;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * API response for a sold item.
 */
@Getter
@Setter
public class SaleItemResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineAmount;
}
