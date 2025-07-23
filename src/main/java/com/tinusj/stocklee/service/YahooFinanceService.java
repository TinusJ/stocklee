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
     * Fetch current stock price for given symbol from Yahoo Finance API.
     */
    @Override
    public Optional<BigDecimal> getPrice(String symbol) {
        try {
            if (!isValidSymbol(symbol)) {
                log.warn("Invalid symbol: {}", symbol);
                return Optional.empty();
            }

            String url = String.format("https://query1.finance.yahoo.com/v8/finance/chart/%s", 
                symbol.toUpperCase());

            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpGet request = new HttpGet(url);
                request.setHeader("User-Agent", "Stocklee/1.0");

                HttpClientResponseHandler<String> responseHandler = response -> {
                    int status = response.getCode();
                    if (status >= 200 && status < 300) {
                        HttpEntity entity = response.getEntity();
                        return entity != null ? EntityUtils.toString(entity) : null;
                    } else {
                        log.warn("Yahoo Finance API returned status: {}", status);
                        return null;
                    }
                };

                String responseBody = httpClient.execute(request, responseHandler);
                if (responseBody != null) {
                    return parsePrice(responseBody);
                }
            }
        } catch (Exception e) {
            log.error("Error fetching price from Yahoo Finance for symbol: {}", symbol, e);
        }
        
        return Optional.empty();
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
     */
    public Optional<StockInfo> getStockInfo(String symbol) {
        try {
            if (!isValidSymbol(symbol)) {
                return Optional.empty();
            }
            
            String upperSymbol = symbol.toUpperCase();
            Optional<BigDecimal> priceOpt = getPrice(upperSymbol);
            
            if (priceOpt.isEmpty()) {
                return Optional.empty();
            }
            
            BigDecimal price = priceOpt.get();
            String name = generateStockName(upperSymbol);
            String description = "Stock information for " + upperSymbol;
            
            return Optional.of(new StockInfo(upperSymbol, name, price, description));
        } catch (Exception e) {
            log.error("Error fetching stock info for symbol: {}", symbol, e);
            return Optional.empty();
        }
    }

    /**
     * Parse price from Yahoo Finance API response.
     */
    private Optional<BigDecimal> parsePrice(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            
            JsonNode chart = root.get("chart");
            if (chart == null || !chart.has("result") || chart.get("result").isEmpty()) {
                log.warn("Invalid response format from Yahoo Finance API");
                return Optional.empty();
            }
            
            JsonNode result = chart.get("result").get(0);
            JsonNode meta = result.get("meta");
            
            if (meta != null && meta.has("regularMarketPrice")) {
                double price = meta.get("regularMarketPrice").asDouble();
                return Optional.of(BigDecimal.valueOf(price).setScale(2, java.math.RoundingMode.HALF_UP));
            } else {
                log.warn("No price data found in Yahoo Finance response");
                return Optional.empty();
            }
            
        } catch (Exception e) {
            log.error("Error parsing Yahoo Finance response", e);
            return Optional.empty();
        }
    }

    /**
     * Mock price generator for fallback purposes.
     * Used only when real API calls fail.
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
     */
    @Override
    public boolean isValidSymbol(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            return false;
        }
        
        String upperSymbol = symbol.toUpperCase().trim();
        
        // Yahoo Finance supports symbols with 1-5 characters for most stocks
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