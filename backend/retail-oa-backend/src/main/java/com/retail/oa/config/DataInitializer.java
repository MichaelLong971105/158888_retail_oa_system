package com.retail.oa.config;

import com.retail.oa.entity.User;
import com.retail.oa.entity.UserRole;
import com.retail.oa.repository.UserRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Creates a bootstrap administrator account when the database is empty.
 */
@Configuration
public class DataInitializer {

    @Bean
    public ApplicationRunner bootstrapAdminUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.countByRoleAndEnabledTrue(UserRole.ADMIN) > 0) {
                return;
            }

            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@retail.local");
            admin.setPassword(passwordEncoder.encode("Admin@123456"));
            admin.setRole(UserRole.ADMIN);
            admin.setEnabled(true);

            userRepository.save(admin);
        };
    }
}
