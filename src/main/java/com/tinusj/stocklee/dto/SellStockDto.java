package com.tinusj.stocklee.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for selling stocks.
 */
@Data
@NoArgsConstructor
public class SellStockDto {

    @NotNull(message = "Owned stock ID is required")
    private UUID ownedStockId;

    @NotNull(message = "Quantity to sell is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}