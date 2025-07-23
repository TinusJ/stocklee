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
public class YahooFinanceService {

    private final ObjectMapper objectMapper;

    /**
     * Fetch current stock price for given symbol.
     * For demo purposes, we'll return a mock price since Yahoo Finance API requires authentication.
     */
    public Optional<BigDecimal> getCurrentPrice(String symbol) {
        try {
            // For demo purposes, return mock prices based on symbol
            return Optional.of(getMockPrice(symbol));
        } catch (Exception e) {
            log.error("Error fetching price for symbol: {}", symbol, e);
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
        return BigDecimal.valueOf(basePrice + variation).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Validate if a stock symbol exists.
     * For demo purposes, we'll consider common symbols as valid.
     */
    public boolean isValidSymbol(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            return false;
        }
        
        String upperSymbol = symbol.toUpperCase().trim();
        
        // For demo, consider symbols with 1-5 characters as valid
        return upperSymbol.matches("^[A-Z]{1,5}$");
    }
}