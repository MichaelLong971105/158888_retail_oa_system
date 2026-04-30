package com.retail.oa.service;

/**
 * @program: retail-oa-backend
 * @description:
 * @author: MichaelLong
 * @create: 2026-03-14 22:34
 **/

import com.retail.oa.dto.user.UserRequest;
import com.retail.oa.dto.user.UserResponse;
import com.retail.oa.entity.User;
import com.retail.oa.entity.UserRole;
import com.retail.oa.exception.DuplicateResourceException;
import com.retail.oa.exception.InvalidOperationException;
import com.retail.oa.exception.ResourceNotFoundException;
import com.retail.oa.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles user CRUD operations and duplicate checks.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Returns all users.
     */
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Returns one user by id.
     */
    public UserResponse getUserById(Long id) {
        return toResponse(getUserEntity(id));
    }

    /**
     * Creates a new user after validating username and email uniqueness.
     */
    public UserResponse createUser(UserRequest request) {
        validateUniqueFields(request, null);

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new InvalidOperationException("Password is required when creating a user");
        }

        User user = new User();
        applyRequest(user, request, true);

        return toResponse(userRepository.save(user));
    }

    /**
     * Updates an existing user while preserving uniqueness constraints.
     */
    public UserResponse updateUser(Long id, UserRequest request) {
        User existingUser = getUserEntity(id);
        validateUniqueFields(request, id);
        ensureAdminSafeguards(existingUser, request);

        applyRequest(existingUser, request, false);

        return toResponse(userRepository.save(existingUser));
    }

    /**
     * Deletes a user by id.
     */
    public void deleteUser(Long id) {
        User existingUser = getUserEntity(id);

        if (existingUser.getRole() == UserRole.ADMIN
                && existingUser.isEnabled()
                && userRepository.countByRoleAndEnabledTrue(UserRole.ADMIN) <= 1) {
            throw new InvalidOperationException("At least one enabled admin account must remain");
        }

        userRepository.delete(existingUser);
    }

    private User getUserEntity(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    private void validateUniqueFields(UserRequest request, Long currentUserId) {
        userRepository.findByUsername(request.getUsername().trim())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(currentUserId)) {
                        throw new DuplicateResourceException("Username already exists");
                    }
                });

        userRepository.findByEmail(request.getEmail().trim())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(currentUserId)) {
                        throw new DuplicateResourceException("Email already exists");
                    }
                });
    }

    private void ensureAdminSafeguards(User existingUser, UserRequest request) {
        boolean removingOnlyAdmin = existingUser.getRole() == UserRole.ADMIN
                && existingUser.isEnabled()
                && userRepository.countByRoleAndEnabledTrue(UserRole.ADMIN) <= 1
                && (request.getRole() != UserRole.ADMIN || !Boolean.TRUE.equals(request.getEnabled()));

        if (removingOnlyAdmin) {
            throw new InvalidOperationException("At least one enabled admin account must remain");
        }
    }

    private void applyRequest(User user, UserRequest request, boolean creating) {
        user.setUsername(request.getUsername().trim());
        user.setEmail(request.getEmail().trim());
        user.setRole(request.getRole());
        user.setEnabled(Boolean.TRUE.equals(request.getEnabled()));
        user.setAdditionalPermissions(
                request.getAdditionalPermissions() == null ? new HashSet<>() : new HashSet<>(request.getAdditionalPermissions())
        );

        if (creating || (request.getPassword() != null && !request.getPassword().isBlank())) {
            user.setPassword(passwordEncoder.encode(request.getPassword().trim()));
        }
    }

    private UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setEnabled(user.isEnabled());
        response.setAdditionalPermissions(new HashSet<>(user.getAdditionalPermissions()));
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }
}
