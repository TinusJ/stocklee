package com.tinusj.stocklee.config;

import com.tinusj.stocklee.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
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
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable())) // For H2 console
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(authz -> authz
                // Allow public access to auth pages
                .requestMatchers("/login", "/register", "/users/create").permitAll()
                // Allow signup without authentication (REST API)
                .requestMatchers(HttpMethod.POST, "/signup").permitAll()
                // Admin endpoints require ROLE_ADMIN
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // Admin-only web endpoints
                .requestMatchers("/user-profiles/**").hasRole("ADMIN")
                .requestMatchers("/history-logs/**").hasRole("ADMIN")
                .requestMatchers("/stock-history/**").hasRole("ADMIN")
                // User endpoints - accessible to authenticated users (both USER and ADMIN)
                .requestMatchers("/profile").authenticated()
                .requestMatchers("/dashboard").authenticated()
                .requestMatchers("/stocks/**").authenticated()
                .requestMatchers("/transactions/**").authenticated()
                .requestMatchers("/owned-stocks/**").authenticated()
                .requestMatchers("/stock-transactions/**").authenticated()
                // Admin-only transaction history endpoints
                .requestMatchers("/transaction-history/**").hasRole("ADMIN")
                // Stock API endpoints require authentication
                .requestMatchers("/api/stocks/**").authenticated()
                // Admin-only API endpoints
                .requestMatchers("/api/user-profiles/**").hasRole("ADMIN")
                .requestMatchers("/api/history-logs/**").hasRole("ADMIN")
                .requestMatchers("/api/stock-histories/**").hasRole("ADMIN")
                .requestMatchers("/api/transaction-histories/**").hasRole("ADMIN")
                // All other API endpoints require authentication
                .requestMatchers("/api/**").authenticated()
                // Allow public access to static resources, root, and actuator health
                .requestMatchers("/", "/static/**", "/actuator/health").permitAll()  
                // Allow access to H2 console for development
                .requestMatchers("/h2-console/**").permitAll()
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .httpBasic(httpBasic -> {}); // Keep HTTP Basic for API endpoints

        return http.build();
    }
}