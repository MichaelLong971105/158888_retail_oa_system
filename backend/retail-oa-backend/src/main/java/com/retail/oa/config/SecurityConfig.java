package com.retail.oa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.retail.oa.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.List;

/**
 * Security configuration for session-based login and role-based access control.
 */
@Configuration
public class SecurityConfig {

    private final UserRepository userRepository;

    public SecurityConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Defines the HTTP security rules used by the application.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {})
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/login").permitAll()
                        .requestMatchers("/auth/me", "/auth/logout").authenticated()
                        // Roles provide the normal path through the UI; explicit permissions let an admin delegate
                        // one module without promoting the user to a broader role.
                        .requestMatchers("/users/**").hasAnyAuthority("ROLE_ADMIN", "MANAGE_USERS")
                        .requestMatchers("/suppliers/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER", "MANAGE_SUPPLIERS")
                        .requestMatchers("/orders/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER", "MANAGE_ORDERS")
                        .requestMatchers(HttpMethod.PUT, "/attendance/schedules/week").hasAnyAuthority(
                                "ROLE_ADMIN", "ROLE_MANAGER", "MANAGE_ATTENDANCE"
                        )
                        .requestMatchers(HttpMethod.POST, "/attendance/leave-requests/*/approve", "/attendance/leave-requests/*/reject")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER", "APPROVE_LEAVE", "MANAGE_ATTENDANCE")
                        .requestMatchers(HttpMethod.POST, "/attendance/punch-records").hasAnyAuthority(
                                "ROLE_ADMIN", "ROLE_MANAGER", "MANAGE_ATTENDANCE"
                        )
                        .requestMatchers(HttpMethod.GET, "/attendance/**").hasAnyAuthority(
                                "ROLE_ADMIN", "ROLE_MANAGER", "ROLE_STAFF", "VIEW_ATTENDANCE", "MANAGE_ATTENDANCE", "APPROVE_LEAVE"
                        )
                        .requestMatchers("/attendance/**").authenticated()
                        .requestMatchers("/products/**").hasAnyAuthority(
                                "ROLE_ADMIN", "ROLE_MANAGER", "ROLE_STAFF", "MANAGE_PRODUCTS", "MANAGE_INVENTORY"
                        )
                        .requestMatchers(HttpMethod.GET, "/sales/**").hasAnyAuthority(
                                "ROLE_ADMIN", "ROLE_MANAGER", "VIEW_SALES", "MANAGE_SALES"
                        )
                        .requestMatchers("/sales/**").hasAnyAuthority(
                                "ROLE_ADMIN", "ROLE_MANAGER", "MANAGE_SALES", "MANAGE_POS"
                        )
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\":\"Unauthorized\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\":\"Forbidden\"}");
                        })
                )
                // The Vue client performs a JSON login and keeps the Spring session cookie.
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByUsername(username)
                .map(user -> {
                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
                    // Spring Security treats roles and permissions as the same authority collection here.
                    user.getAdditionalPermissions()
                            .forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission.name())));

                    UserDetails userDetails = User.withUsername(user.getUsername())
                            .password(user.getPassword())
                            .authorities(authorities)
                            .disabled(!user.isEnabled())
                            .build();
                    return userDetails;
                })
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        BCryptPasswordEncoder delegate = new BCryptPasswordEncoder();
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return delegate.encode(rawPassword);
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                if (encodedPassword == null) {
                    return false;
                }

                if (encodedPassword.equals(rawPassword.toString())) {
                    // Kept for older local seed data that may still contain plain-text passwords.
                    return true;
                }

                try {
                    return delegate.matches(rawPassword, encodedPassword);
                } catch (IllegalArgumentException ex) {
                    return false;
                }
            }
        };
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
