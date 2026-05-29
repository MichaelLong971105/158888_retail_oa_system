package com.retail.oa.dto.supplier;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Response DTO returned by supplier APIs.
 */
@Getter
@Setter
public class SupplierResponse {
    private Long id;
    private String name;
    private String contactPerson;
    private String phone;
    private String email;
    private String address;
    private String remark;
    private LocalDateTime createdAt;
}
