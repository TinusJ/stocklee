package com.tinusj.stocklee.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for dashboard portfolio summary.
 */
@Data
@NoArgsConstructor
public class PortfolioSummaryDto {
    
    private BigDecimal totalInvestment;
    private BigDecimal currentValue;
    private BigDecimal profitLoss;
    private BigDecimal availableBalance;
    private List<OwnedStockDto> ownedStocks;
    
    public BigDecimal getProfitLossPercentage() {
        if (totalInvestment == null || totalInvestment.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return profitLoss.divide(totalInvestment, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
}