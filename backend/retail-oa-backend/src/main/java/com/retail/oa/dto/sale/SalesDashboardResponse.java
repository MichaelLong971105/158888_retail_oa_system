package com.retail.oa.dto.sale;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Aggregated sales data used by the dashboard page.
 */
@Getter
@Setter
public class SalesDashboardResponse {

    private BigDecimal todaySalesAmount;
    private long todaySalesCount;
    private List<TopSellingProductResponse> topSellingProducts = new ArrayList<>();
}
