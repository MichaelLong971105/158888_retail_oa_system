package com.retail.oa.dto.user;

import com.retail.oa.entity.UserPermission;
import com.retail.oa.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Request payload for user create and update operations.
 */
@Getter
@Setter
public class UserRequest {

    @NotBlank(message = "Username cannot be empty")
    private String username;

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Email format is invalid")
    private String email;

    private String password;

    @NotNull(message = "Role cannot be null")
    private UserRole role;

    @NotNull(message = "Enabled flag cannot be null")
    private Boolean enabled;

    private Set<UserPermission> additionalPermissions = new HashSet<>();
}
