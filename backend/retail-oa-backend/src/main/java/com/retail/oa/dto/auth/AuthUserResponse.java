package com.retail.oa.dto.auth;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class AuthUserResponse {

    private Long id;
    private String username;
    private String email;
    private String role;
    private boolean enabled;
    private Set<String> permissions = new HashSet<>();
}
