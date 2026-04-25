package com.retail.oa.dto;

/**
 * @program: retail-oa-backend
 * @description:
 * @author: MichaelLong
 * @create: 2026-04-11 18:48
 **/
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
