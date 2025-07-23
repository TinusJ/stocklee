package com.tinusj.stocklee.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for StockHistory entity with validation.
 */
@Data
@NoArgsConstructor
public class StockHistoryDto {

    private UUID id;

    @NotNull(message = "Stock is required")
    private UUID stockId;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Price must be a valid monetary amount")
    private BigDecimal price;

    private LocalDateTime timestamp;

    // Helper fields for display
    private String stockSymbol;
    private String stockName;
}