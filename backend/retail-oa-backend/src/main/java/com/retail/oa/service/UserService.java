package com.retail.oa.service;

/**
 * @program: retail-oa-backend
 * @description:
 * @author: MichaelLong
 * @create: 2026-03-14 22:34
 **/

import com.retail.oa.entity.User;
import com.retail.oa.exception.DuplicateResourceException;
import com.retail.oa.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Handles user CRUD operations and duplicate checks.
 */
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Returns all users.
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Returns one user by id.
     */
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Creates a new user after validating username and email uniqueness.
     */
    public User createUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new DuplicateResourceException("Username already exists");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }
        return userRepository.save(user);
    }

    /**
     * Updates an existing user while preserving uniqueness constraints.
     */
    public Optional<User> updateUser(Long id, User updatedUser) {
        return userRepository.findById(id).map(existingUser -> {
            if (!existingUser.getUsername().equals(updatedUser.getUsername())
                    && userRepository.existsByUsername(updatedUser.getUsername())) {
                throw new DuplicateResourceException("Username already exists");
            }

            if (!existingUser.getEmail().equals(updatedUser.getEmail())
                    && userRepository.existsByEmail(updatedUser.getEmail())) {
                throw new DuplicateResourceException("Email already exists");
            }

            existingUser.setUsername(updatedUser.getUsername());
            existingUser.setEmail(updatedUser.getEmail());
            existingUser.setPassword(updatedUser.getPassword());
            existingUser.setRole(updatedUser.getRole());

            return userRepository.save(existingUser);
        });
    }

    /**
     * Deletes a user by id and returns whether the deletion was performed.
     */
    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

}
