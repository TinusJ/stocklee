package com.tinusj.stocklee.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for current stock price information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrentPriceDto {
    private String symbol;
    private String name;
    private BigDecimal currentPrice;
    private BigDecimal previousPrice;
    private boolean isLive; // True if from database, false if fetched from API
}