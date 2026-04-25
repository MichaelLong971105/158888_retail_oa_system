package com.retail.oa.dto;

/**
 * @program: retail-oa-backend
 * @description:
 * @author: MichaelLong
 * @create: 2026-04-11 18:36
 **/

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
