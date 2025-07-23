package com.tinusj.stocklee.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for HistoryLog entity with validation.
 */
@Data
@NoArgsConstructor
public class HistoryLogDto {

    private UUID id;

    @NotNull(message = "User is required")
    private UUID userId;

    @NotBlank(message = "Action is required")
    @Size(min = 1, max = 500, message = "Action must be between 1 and 500 characters")
    private String action;

    private LocalDateTime timestamp;

    // Helper field for display
    private String username;
}