package com.retail.oa.dto.sale;

import com.retail.oa.entity.SaleSource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Request payload used by manual sales and future POS integration.
 */
@Getter
@Setter
public class SaleIngestRequest {

    private SaleSource source;
    private String externalSaleId;
    private Long cashierUserId;
    private String cashierName;
    private LocalDateTime saleTime;
    private String remark;

    @NotEmpty(message = "Sale items cannot be empty")
    @Valid
    private List<SaleItemRequest> items;
}
