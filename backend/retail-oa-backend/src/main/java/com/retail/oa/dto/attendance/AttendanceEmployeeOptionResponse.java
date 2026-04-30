package com.retail.oa.dto.attendance;

import com.retail.oa.entity.UserRole;
import lombok.Getter;
import lombok.Setter;

/**
 * Lightweight user option for attendance pages.
 */
@Getter
@Setter
public class AttendanceEmployeeOptionResponse {

    private Long id;
    private String username;
    private String email;
    private UserRole role;
}
