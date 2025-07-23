package com.tinusj.stocklee.service;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Interface for stock price providers.
 * Allows multiple implementations for different financial data sources.
 */
public interface StockPriceProvider {
    
    /**
     * Get current stock price for the given symbol.
     * 
     * @param symbol Stock symbol (e.g., "AAPL", "MSFT")
     * @return Current price or empty if not available
     */
    Optional<BigDecimal> getPrice(String symbol);
    
    /**
     * Check if the given stock symbol is valid.
     * 
     * @param symbol Stock symbol to validate
     * @return true if valid, false otherwise
     */
    boolean isValidSymbol(String symbol);
}