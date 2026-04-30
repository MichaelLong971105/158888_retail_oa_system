package com.retail.oa.service;

import com.retail.oa.dto.auth.AuthUserResponse;
import com.retail.oa.entity.User;
import com.retail.oa.exception.ResourceNotFoundException;
import com.retail.oa.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public AuthUserResponse getCurrentUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResourceNotFoundException("Current user not found");
        }

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

        return toAuthUserResponse(user);
    }

    public AuthUserResponse toAuthUserResponse(User user) {
        AuthUserResponse response = new AuthUserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().name());
        response.setEnabled(user.isEnabled());
        response.getPermissions().addAll(
                user.getAdditionalPermissions().stream().map(Enum::name).toList()
        );
        return response;
    }
}
