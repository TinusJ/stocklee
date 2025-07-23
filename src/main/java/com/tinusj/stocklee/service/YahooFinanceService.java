package com.tinusj.stocklee.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Service to fetch stock prices from Yahoo Finance API.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class YahooFinanceService implements StockPriceProvider {

    private final ObjectMapper objectMapper;

    /**
     * Fetch current stock price for given symbol.
     * For demo purposes, we'll return a mock price since Yahoo Finance API requires authentication.
     * In a production environment, this would integrate with a real financial data API.
     */
    @Override
    public Optional<BigDecimal> getPrice(String symbol) {
        try {
            // For demo purposes, return mock prices based on symbol
            return Optional.of(getMockPrice(symbol));
        } catch (Exception e) {
            log.error("Error fetching price for symbol: {}", symbol, e);
            return Optional.empty();
        }
    }

    /**
     * Fetch current stock price for given symbol.
     * This method provides backward compatibility and delegates to getPrice().
     */
    public Optional<BigDecimal> getCurrentPrice(String symbol) {
        return getPrice(symbol);
    }

    /**
     * Fetch stock information including name and current price.
     * In a real implementation, this would call a financial data API.
     */
    public Optional<StockInfo> getStockInfo(String symbol) {
        try {
            if (!isValidSymbol(symbol)) {
                return Optional.empty();
            }
            
            String upperSymbol = symbol.toUpperCase();
            BigDecimal price = getMockPrice(upperSymbol);
            String name = generateStockName(upperSymbol);
            String description = "Stock information for " + upperSymbol;
            
            return Optional.of(new StockInfo(upperSymbol, name, price, description));
        } catch (Exception e) {
            log.error("Error fetching stock info for symbol: {}", symbol, e);
            return Optional.empty();
        }
    }

    /**
     * Mock price generator for demo purposes.
     * In a real implementation, this would call Yahoo Finance API.
     */
    private BigDecimal getMockPrice(String symbol) {
        // Generate mock prices based on symbol hash for consistency
        int hash = Math.abs(symbol.hashCode());
        double basePrice = 50.0 + (hash % 200); // Price between 50-250
        double variation = Math.sin(System.currentTimeMillis() / 10000.0) * 5; // Small variation
        return BigDecimal.valueOf(basePrice + variation).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Validate if a stock symbol exists.
     * For demo purposes, we'll consider common symbols as valid.
     */
    @Override
    public boolean isValidSymbol(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            return false;
        }
        
        String upperSymbol = symbol.toUpperCase().trim();
        
        // For demo, consider symbols with 1-5 characters as valid
        return upperSymbol.matches("^[A-Z]{1,5}$");
    }

    /**
     * Generate a mock stock name based on symbol.
     */
    private String generateStockName(String symbol) {
        // Some well-known stock names for demo
        return switch (symbol.toUpperCase()) {
            case "AAPL" -> "Apple Inc.";
            case "MSFT" -> "Microsoft Corporation";
            case "GOOGL" -> "Alphabet Inc.";
            case "AMZN" -> "Amazon.com Inc.";
            case "TSLA" -> "Tesla Inc.";
            case "META" -> "Meta Platforms Inc.";
            case "NVDA" -> "NVIDIA Corporation";
            case "JPM" -> "JPMorgan Chase & Co.";
            case "JNJ" -> "Johnson & Johnson";
            case "V" -> "Visa Inc.";
            default -> symbol + " Corporation";
        };
    }

    /**
     * Data class for stock information.
     */
    public static class StockInfo {
        private final String symbol;
        private final String name;
        private final BigDecimal price;
        private final String description;

        public StockInfo(String symbol, String name, BigDecimal price, String description) {
            this.symbol = symbol;
            this.name = name;
            this.price = price;
            this.description = description;
        }

        public String getSymbol() { return symbol; }
        public String getName() { return name; }
        public BigDecimal getPrice() { return price; }
        public String getDescription() { return description; }
    }
}