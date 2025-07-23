package com.tinusj.stocklee.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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
    @DecimalMin(value = "0.0001", message = "Quantity must be greater than 0")
    @Digits(integer = 10, fraction = 4, message = "Quantity must be a valid number with up to 4 decimal places")
    private BigDecimal quantity;
}