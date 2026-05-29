package com.retail.oa.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Request DTO used to create a purchase order.
 */
@Getter
@Setter
public class OrderRequest {

    @NotEmpty(message = "Order items cannot be empty")
    @Valid
    private List<OrderItemRequest> items;

    @NotNull(message = "Supplier id cannot be null")
    private Long supplierId;
}
