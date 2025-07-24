package com.tinusj.stocklee.config;

import com.tinusj.stocklee.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the application.
 */
@Configuration
@EnableWebSecurity
@Profile("!test")
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // Allow signup without authentication
                .requestMatchers(HttpMethod.POST, "/signup").permitAll()
                // Admin endpoints require ROLE_ADMIN
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // User endpoint requires authentication (ROLE_USER or ROLE_ADMIN)
                .requestMatchers("/user").authenticated()
                // Stock endpoints require authentication
                .requestMatchers("/stocks/**").authenticated()
                // All other API endpoints require authentication
                .requestMatchers("/api/**").authenticated()
                // Allow public access to static resources and actuator health
                .requestMatchers("/", "/static/**", "/actuator/health").permitAll()
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .httpBasic(httpBasic -> {})
            .authenticationProvider(authenticationProvider());

        return http.build();
    }
}