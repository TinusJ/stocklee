package com.tinusj.stocklee.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Composite stock price provider that uses multiple APIs with fallback logic.
 * First attempts Yahoo Finance, then falls back to AlphaVantage if Yahoo fails.
 */
@Service
@Primary
@RequiredArgsConstructor
@Slf4j
public class CompositeStockPriceProvider implements StockPriceProvider {

    private final YahooFinanceService yahooFinanceService;
    private final AlphaVantageService alphaVantageService;

    @Override
    public Optional<BigDecimal> getPrice(String symbol) {
        if (!isValidSymbol(symbol)) {
            log.warn("Invalid symbol: {}", symbol);
            return Optional.empty();
        }

        // First try Yahoo Finance
        log.debug("Attempting to fetch price for {} from Yahoo Finance", symbol);
        Optional<BigDecimal> yahooPrice = yahooFinanceService.getPrice(symbol);
        
        if (yahooPrice.isPresent()) {
            log.debug("Successfully fetched price for {} from Yahoo Finance: {}", symbol, yahooPrice.get());
            return yahooPrice;
        }

        // Fallback to AlphaVantage
        log.debug("Yahoo Finance failed for {}, trying AlphaVantage", symbol);
        Optional<BigDecimal> alphaPrice = alphaVantageService.getPrice(symbol);
        
        if (alphaPrice.isPresent()) {
            log.debug("Successfully fetched price for {} from AlphaVantage: {}", symbol, alphaPrice.get());
            return alphaPrice;
        }

        log.warn("Failed to fetch price for {} from both Yahoo Finance and AlphaVantage", symbol);
        return Optional.empty();
    }

    @Override
    public boolean isValidSymbol(String symbol) {
        // Use Yahoo Finance validation as primary
        return yahooFinanceService.isValidSymbol(symbol);
    }

    /**
     * Get stock information, preferring Yahoo Finance.
     */
    public Optional<YahooFinanceService.StockInfo> getStockInfo(String symbol) {
        // Yahoo Finance provides more comprehensive stock info
        return yahooFinanceService.getStockInfo(symbol);
    }
}