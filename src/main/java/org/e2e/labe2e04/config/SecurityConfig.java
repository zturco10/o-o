package org.e2e.labe2e04.config;

import org.e2e.labe2e04.security.filter.JwtAuthFilter;
import org.e2e.labe2e04.user.domain.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

public class SecurityConfig {
    private final JwtAuthFilter jwtAuthFilter;

    private final UserService userService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
    }

    @Bean
    static RoleHierarchy roleHierarchy() {
    }

    @Bean
    static MethodSecurityExpressionHandler methodSecurityExpressionHandler(RoleHierarchy roleHierarchy) {
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
    }
}
