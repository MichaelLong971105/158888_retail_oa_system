package com.retail.oa.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse {

    private String message;
    private AuthUserResponse user;
}
