package com.retail.oa.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Response DTO for order statistics.
 */
@Getter
@Setter
public class OrderStatsResponse {

    private long totalOrders;
    private long pendingOrders;
    private long receivedOrders;
    private long cancelledOrders;
    private BigDecimal totalAmount;
    private BigDecimal receivedAmount;
}
