package com.tinusj.stocklee.dto;

import com.tinusj.stocklee.entity.Stock;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for Stock entity with validation.
 */
@Data
@NoArgsConstructor
public class StockDto {

    private UUID id;

    @NotBlank(message = "Stock symbol is required")
    @Size(min = 1, max = 10, message = "Stock symbol must be between 1 and 10 characters")
    @Pattern(regexp = "^[A-Z0-9]+$", message = "Stock symbol must contain only uppercase letters and numbers")
    private String symbol;

    @NotBlank(message = "Stock name is required")
    @Size(min = 2, max = 100, message = "Stock name must be between 2 and 100 characters")
    private String name;

    @NotNull(message = "Current price is required")
    @DecimalMin(value = "0.01", message = "Current price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Current price must be a valid monetary amount")
    private BigDecimal currentPrice;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotNull(message = "Market type is required")
    private Stock.MarketType market;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}