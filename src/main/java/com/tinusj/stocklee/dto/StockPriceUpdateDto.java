package com.tinusj.stocklee.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for real-time stock price updates via WebSocket.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockPriceUpdateDto {
    private String symbol;
    private String name;
    private BigDecimal currentPrice;
    private BigDecimal previousPrice;
    private BigDecimal priceChange;
    private BigDecimal priceChangePercentage;
    private LocalDateTime timestamp;
    
    public StockPriceUpdateDto(String symbol, String name, BigDecimal currentPrice, BigDecimal previousPrice) {
        this.symbol = symbol;
        this.name = name;
        this.currentPrice = currentPrice;
        this.previousPrice = previousPrice;
        this.timestamp = LocalDateTime.now();
        
        // Calculate price change and percentage
        if (previousPrice != null && previousPrice.compareTo(BigDecimal.ZERO) > 0) {
            this.priceChange = currentPrice.subtract(previousPrice);
            this.priceChangePercentage = priceChange
                    .divide(previousPrice, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        } else {
            this.priceChange = BigDecimal.ZERO;
            this.priceChangePercentage = BigDecimal.ZERO;
        }
    }
}