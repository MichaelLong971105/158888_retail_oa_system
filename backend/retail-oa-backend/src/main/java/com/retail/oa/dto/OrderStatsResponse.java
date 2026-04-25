package com.retail.oa.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * @program: retail-oa-backend
 * @description:
 * @author: MichaelLong
 * @create: 2026-04-11 21:45
 **/

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
