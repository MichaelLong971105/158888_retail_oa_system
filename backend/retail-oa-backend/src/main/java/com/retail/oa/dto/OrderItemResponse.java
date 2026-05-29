package com.retail.oa.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Response DTO for one purchase order item.
 */
@Getter
@Setter
public class OrderItemResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String sku;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal subtotal;
}
