package com.retail.oa.dto.user;

import com.retail.oa.entity.UserPermission;
import com.retail.oa.entity.UserRole;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * API response for user management.
 */
@Getter
@Setter
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private UserRole role;
    private boolean enabled;
    private Set<UserPermission> additionalPermissions = new HashSet<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
