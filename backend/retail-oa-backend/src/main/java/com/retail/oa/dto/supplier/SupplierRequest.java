package com.retail.oa.dto.supplier;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Request DTO used for supplier create and update operations.
 */
@Getter
@Setter
public class SupplierRequest {

    @NotBlank(message = "Supplier name cannot be blank")
    @Size(max = 100, message = "Supplier name cannot exceed 100 characters")
    private String name;

    @Size(max = 100, message = "Contact person cannot exceed 100 characters")
    private String contactPerson;

    @Size(max = 20, message = "Phone cannot exceed 20 characters")
    private String phone;

    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    @Size(max = 255, message = "Address cannot exceed 255 characters")
    private String address;

    @Size(max = 255, message = "Remark cannot exceed 255 characters")
    private String remark;
}
