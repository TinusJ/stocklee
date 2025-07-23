package com.tinusj.stocklee.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for buying stocks.
 */
@Data
@NoArgsConstructor
public class BuyStockDto {

    @NotBlank(message = "Stock symbol is required")
    @Pattern(regexp = "^[A-Z0-9]{1,5}$", message = "Stock symbol must be 1-5 uppercase letters or numbers")
    private String symbol;

    @NotNull(message = "Investment amount is required")
    @DecimalMin(value = "0.01", message = "Investment amount must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Investment amount must be a valid monetary amount")
    private BigDecimal investmentAmount;

    /**
     * Optional purchase price override. If not provided, current market price will be used.
     */
    @DecimalMin(value = "0.01", message = "Purchase price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Purchase price must be a valid monetary amount")
    private BigDecimal purchasePrice;
}