package com.retail.oa.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthUserResponse {

    private Long id;
    private String username;
    private String email;
    private String role;
}
