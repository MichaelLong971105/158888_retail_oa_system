package com.retail.oa.dto.sale;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Request payload for generating fake sales records during testing.
 */
@Getter
@Setter
public class MockSalesRequest {

    @Min(value = 1, message = "Count must be greater than 0")
    private Integer count = 5;

    private LocalDate saleDate;
    private Long cashierUserId;
}
