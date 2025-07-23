package com.tinusj.stocklee.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Data Transfer Object for OwnedStock entity with additional fields for dashboard display.
 */
@Data
@NoArgsConstructor
public class OwnedStockDto {

    private UUID id;
    private BigDecimal quantity;
    private BigDecimal averagePrice;
    private BigDecimal totalValue;
    
    // Stock details
    private String stockSymbol;
    private String stockName;
    private BigDecimal currentPrice;
    
    // Calculated fields
    private BigDecimal currentValue;
    private BigDecimal profitLoss;
    
    public BigDecimal getProfitLossPercentage() {
        if (totalValue == null || totalValue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return profitLoss.divide(totalValue, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
}