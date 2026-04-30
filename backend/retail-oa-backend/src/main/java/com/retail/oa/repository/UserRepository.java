package com.retail.oa.repository;

/**
 * @program: retail-oa-backend
 * @description:
 * @author: MichaelLong
 * @create: 2026-03-14 22:32
 **/

import com.retail.oa.entity.User;
import com.retail.oa.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Repository for user persistence and duplicate checks.
 */
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByRole(UserRole role);

    List<User> findByEnabledTrue();

    long countByRoleAndEnabledTrue(UserRole role);

    List<User> findByRoleInAndEnabledTrue(Collection<UserRole> roles);
}
