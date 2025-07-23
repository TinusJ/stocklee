package com.tinusj.stocklee.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for UserProfile entity with validation.
 */
@Data
@NoArgsConstructor
public class UserProfileDto {

    private UUID id;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    @DecimalMin(value = "0.00", message = "Balance cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Balance must be a valid monetary amount")
    private BigDecimal balance;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}