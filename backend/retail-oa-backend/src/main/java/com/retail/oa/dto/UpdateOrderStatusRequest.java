package com.retail.oa.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Request DTO for updating a purchase order status.
 */
@Getter
@Setter
public class UpdateOrderStatusRequest {

    @NotBlank(message = "Status cannot be blank")
    private String status;

}
