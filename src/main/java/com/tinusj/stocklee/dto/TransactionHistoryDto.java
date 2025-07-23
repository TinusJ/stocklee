package com.tinusj.stocklee.dto;

import com.tinusj.stocklee.entity.TransactionHistory;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for TransactionHistory entity with validation.
 */
@Data
@NoArgsConstructor
public class TransactionHistoryDto {

    private UUID id;

    @NotNull(message = "Transaction is required")
    private UUID transactionId;

    @NotNull(message = "Status is required")
    private TransactionHistory.TransactionStatus status;

    private LocalDateTime timestamp;

    // Helper fields for display
    private String transactionType;
    private String stockSymbol;
    private String username;
    private Integer quantity;
}