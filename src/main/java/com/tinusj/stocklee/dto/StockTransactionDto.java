package com.tinusj.stocklee.dto;

import com.tinusj.stocklee.entity.StockTransaction;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for StockTransaction entity with validation.
 */
@Data
@NoArgsConstructor
public class StockTransactionDto {

    private UUID id;

    @NotNull(message = "Stock is required")
    private UUID stockId;

    @NotNull(message = "User is required")
    private UUID userId;

    @NotNull(message = "Transaction type is required")
    private StockTransaction.TransactionType transactionType;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 1000000, message = "Quantity cannot exceed 1,000,000")
    private Integer quantity;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Price must be a valid monetary amount")
    private BigDecimal price;

    @NotNull(message = "Total value is required")
    @DecimalMin(value = "0.01", message = "Total value must be greater than 0")
    @Digits(integer = 12, fraction = 2, message = "Total value must be a valid monetary amount")
    private BigDecimal totalValue;

    private LocalDateTime timestamp;

    // Helper fields for display
    private String stockSymbol;
    private String stockName;
    private String username;
}