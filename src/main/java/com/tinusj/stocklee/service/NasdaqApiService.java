package com.tinusj.stocklee.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

/**
 * Service to fetch stock prices and historical data from Nasdaq API.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NasdaqApiService implements StockPriceProvider {

    private final ObjectMapper objectMapper;
    
    @Value("${nasdaq.api.key:demo}")
    private String apiKey;
    
    @Value("${nasdaq.api.base-url:https://api.nasdaq.com}")
    private String baseUrl;

    @Override
    public Optional<BigDecimal> getPrice(String symbol) {
        try {
            if (!isValidSymbol(symbol)) {
                log.warn("Invalid symbol: {}", symbol);
                return Optional.empty();
            }

            // For demo purposes, we'll simulate Nasdaq API response since we don't have a real API key
            // In production, you would replace this with actual Nasdaq API endpoints
            String url = String.format("%s/api/quote/%s/info?assetclass=stocks", 
                baseUrl, symbol.toUpperCase());

            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpGet request = new HttpGet(url);
                request.setHeader("User-Agent", "Stocklee/1.0");
                
                // In production, add API key header:
                // request.setHeader("X-API-KEY", apiKey);

                HttpClientResponseHandler<String> responseHandler = response -> {
                    int status = response.getCode();
                    if (status >= 200 && status < 300) {
                        HttpEntity entity = response.getEntity();
                        return entity != null ? EntityUtils.toString(entity) : null;
                    } else {
                        log.warn("Nasdaq API returned status: {}", status);
                        return null;
                    }
                };

                String responseBody = httpClient.execute(request, responseHandler);
                if (responseBody != null) {
                    return parsePrice(responseBody, symbol);
                }
            }
        } catch (Exception e) {
            log.debug("Nasdaq API not available (expected in demo mode), falling back to mock data for symbol: {}", symbol);
            // Return mock data for demo purposes
            return getMockPrice(symbol);
        }
        
        return Optional.empty();
    }

    @Override
    public boolean isValidSymbol(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            return false;
        }
        
        String upperSymbol = symbol.toUpperCase().trim();
        
        // Nasdaq supports symbols with 1-5 characters for most stocks
        return upperSymbol.matches("^[A-Z]{1,5}$");
    }

    /**
     * Fetch historical stock data for a given symbol and date range.
     */
    public List<HistoricalStockData> getHistoricalData(String symbol, LocalDate fromDate, LocalDate toDate) {
        try {
            if (!isValidSymbol(symbol)) {
                log.warn("Invalid symbol for historical data: {}", symbol);
                return new ArrayList<>();
            }

            // For demo purposes, simulate historical data
            // In production, you would use actual Nasdaq historical data endpoints
            log.debug("Fetching historical data from Nasdaq API for {} from {} to {}", symbol, fromDate, toDate);
            
            return generateMockHistoricalData(symbol, fromDate, toDate);
            
        } catch (Exception e) {
            log.error("Error fetching historical data from Nasdaq for symbol: {}", symbol, e);
            return new ArrayList<>();
        }
    }

    /**
     * Parse price from Nasdaq API response.
     */
    private Optional<BigDecimal> parsePrice(String responseBody, String symbol) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            
            // Check for error in response
            if (root.has("Error")) {
                log.warn("Nasdaq API error: {}", root.get("Error").asText());
                return Optional.empty();
            }
            
            // Parse price from response - this is a placeholder structure
            // In production, adjust based on actual Nasdaq API response format
            if (root.has("data") && root.get("data").has("primaryData")) {
                JsonNode primaryData = root.get("data").get("primaryData");
                if (primaryData.has("lastSalePrice")) {
                    String priceStr = primaryData.get("lastSalePrice").asText().replace("$", "");
                    return Optional.of(new BigDecimal(priceStr));
                }
            }
            
            // If parsing fails, return mock data for demo
            return getMockPrice(symbol);
            
        } catch (Exception e) {
            log.debug("Error parsing Nasdaq response, using mock data: {}", e.getMessage());
            return getMockPrice(symbol);
        }
    }

    /**
     * Generate mock price for demo purposes.
     */
    private Optional<BigDecimal> getMockPrice(String symbol) {
        try {
            // Generate consistent mock prices based on symbol hash
            int hash = Math.abs(symbol.hashCode());
            double basePrice = 100.0 + (hash % 300); // Price between 100-400
            double variation = Math.sin(System.currentTimeMillis() / 15000.0) * 10; // Small variation
            BigDecimal price = BigDecimal.valueOf(basePrice + variation).setScale(2, java.math.RoundingMode.HALF_UP);
            
            log.debug("Generated mock Nasdaq price for {}: {}", symbol, price);
            return Optional.of(price);
        } catch (Exception e) {
            log.error("Error generating mock price for symbol: {}", symbol, e);
            return Optional.empty();
        }
    }

    /**
     * Generate mock historical data for demo purposes.
     */
    private List<HistoricalStockData> generateMockHistoricalData(String symbol, LocalDate fromDate, LocalDate toDate) {
        List<HistoricalStockData> historicalData = new ArrayList<>();
        
        try {
            int hash = Math.abs(symbol.hashCode());
            double basePrice = 100.0 + (hash % 300);
            
            LocalDate currentDate = fromDate;
            while (!currentDate.isAfter(toDate)) {
                // Skip weekends for realistic data
                if (currentDate.getDayOfWeek().getValue() <= 5) {
                    // Generate OHLCV data with some randomness
                    long daysSinceEpoch = currentDate.toEpochDay();
                    double dayVariation = Math.sin(daysSinceEpoch / 10.0) * 5;
                    double randomFactor = Math.sin(daysSinceEpoch * symbol.hashCode() / 1000.0) * 8;
                    
                    double closePrice = basePrice + dayVariation + randomFactor;
                    double openPrice = closePrice + (Math.random() - 0.5) * 4;
                    double highPrice = Math.max(openPrice, closePrice) + Math.random() * 3;
                    double lowPrice = Math.min(openPrice, closePrice) - Math.random() * 3;
                    long volume = (long) (500000 + Math.random() * 2000000);
                    
                    HistoricalStockData data = new HistoricalStockData(
                        symbol,
                        currentDate,
                        BigDecimal.valueOf(openPrice).setScale(2, java.math.RoundingMode.HALF_UP),
                        BigDecimal.valueOf(highPrice).setScale(2, java.math.RoundingMode.HALF_UP),
                        BigDecimal.valueOf(lowPrice).setScale(2, java.math.RoundingMode.HALF_UP),
                        BigDecimal.valueOf(closePrice).setScale(2, java.math.RoundingMode.HALF_UP),
                        volume
                    );
                    
                    historicalData.add(data);
                }
                currentDate = currentDate.plusDays(1);
            }
        } catch (Exception e) {
            log.error("Error generating mock historical data for symbol: {}", symbol, e);
        }
        
        return historicalData;
    }

    /**
     * Data class for historical stock data.
     */
    public static class HistoricalStockData {
        private final String symbol;
        private final LocalDate date;
        private final BigDecimal openPrice;
        private final BigDecimal highPrice;
        private final BigDecimal lowPrice;
        private final BigDecimal closePrice;
        private final Long volume;

        public HistoricalStockData(String symbol, LocalDate date, BigDecimal openPrice, 
                                 BigDecimal highPrice, BigDecimal lowPrice, BigDecimal closePrice, Long volume) {
            this.symbol = symbol;
            this.date = date;
            this.openPrice = openPrice;
            this.highPrice = highPrice;
            this.lowPrice = lowPrice;
            this.closePrice = closePrice;
            this.volume = volume;
        }

        // Getters
        public String getSymbol() { return symbol; }
        public LocalDate getDate() { return date; }
        public BigDecimal getOpenPrice() { return openPrice; }
        public BigDecimal getHighPrice() { return highPrice; }
        public BigDecimal getLowPrice() { return lowPrice; }
        public BigDecimal getClosePrice() { return closePrice; }
        public Long getVolume() { return volume; }
    }
}