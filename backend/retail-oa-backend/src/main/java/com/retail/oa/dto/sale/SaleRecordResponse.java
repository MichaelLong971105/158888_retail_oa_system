package com.retail.oa.dto.sale;

import com.retail.oa.entity.SaleSource;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * API response for a sales record.
 */
@Getter
@Setter
public class SaleRecordResponse {

    private Long id;
    private String saleNumber;
    private String externalSaleId;
    private SaleSource source;
    private Long cashierId;
    private String cashierName;
    private BigDecimal totalAmount;
    private LocalDateTime saleTime;
    private LocalDateTime createdAt;
    private String remark;
    private List<SaleItemResponse> items = new ArrayList<>();
}
