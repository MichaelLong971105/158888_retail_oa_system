package com.retail.oa.dto;

/**
 * @program: retail-oa-backend
 * @description:
 * @author: MichaelLong
 * @create: 2026-04-06 02:00
 **/

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Request DTO used by manual stock in and stock out endpoints.
 */
@Getter
@Setter
public class StockUpdateRequest {

    @NotNull(message = "Quantity cannot be null")
    @Min(value = 1, message = "Quantity must be greater than 0")
    private Integer quantity;

    @Size(max = 255, message = "Remark cannot exceed 255 characters")
    private String remark;

    public StockUpdateRequest() {
    }

    public StockUpdateRequest(Integer quantity, String remark) {
        this.quantity = quantity;
        this.remark = remark;
    }
}
