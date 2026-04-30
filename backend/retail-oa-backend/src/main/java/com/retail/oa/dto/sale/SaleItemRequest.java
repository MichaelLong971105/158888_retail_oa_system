package com.retail.oa.dto.sale;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * One line item in an ingested sale.
 */
@Getter
@Setter
public class SaleItemRequest {

    private Long productId;
    private String sku;
    private String productName;

    @Min(value = 1, message = "Quantity must be greater than 0")
    private Integer quantity;

    @DecimalMin(value = "0.0", inclusive = false, message = "Unit price must be greater than 0")
    private BigDecimal unitPrice;
}
