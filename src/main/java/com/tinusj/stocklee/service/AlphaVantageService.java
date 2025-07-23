package com.tinusj.stocklee.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Service to fetch stock prices from AlphaVantage API.
 * This serves as a fallback when Yahoo Finance is unavailable.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlphaVantageService implements StockPriceProvider {

    private final ObjectMapper objectMapper;
    
    @Value("${alphavantage.api.key:demo}")
    private String apiKey;
    
    private static final String BASE_URL = "https://www.alphavantage.co/query";

    @Override
    public Optional<BigDecimal> getPrice(String symbol) {
        try {
            if (!isValidSymbol(symbol)) {
                log.warn("Invalid symbol: {}", symbol);
                return Optional.empty();
            }

            String url = String.format("%s?function=GLOBAL_QUOTE&symbol=%s&apikey=%s", 
                BASE_URL, symbol.toUpperCase(), apiKey);

            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpGet request = new HttpGet(url);
                request.setHeader("User-Agent", "Stocklee/1.0");

                HttpClientResponseHandler<String> responseHandler = response -> {
                    int status = response.getCode();
                    if (status >= 200 && status < 300) {
                        HttpEntity entity = response.getEntity();
                        return entity != null ? EntityUtils.toString(entity) : null;
                    } else {
                        log.warn("AlphaVantage API returned status: {}", status);
                        return null;
                    }
                };

                String responseBody = httpClient.execute(request, responseHandler);
                if (responseBody != null) {
                    return parsePrice(responseBody);
                }
            }
        } catch (Exception e) {
            log.error("Error fetching price from AlphaVantage for symbol: {}", symbol, e);
        }
        
        return Optional.empty();
    }

    @Override
    public boolean isValidSymbol(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            return false;
        }
        
        String upperSymbol = symbol.toUpperCase().trim();
        
        // AlphaVantage supports symbols with 1-5 characters for most stocks
        return upperSymbol.matches("^[A-Z]{1,5}$");
    }

    /**
     * Parse price from AlphaVantage API response.
     */
    private Optional<BigDecimal> parsePrice(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            
            // Check for error in response
            if (root.has("Error Message")) {
                log.warn("AlphaVantage API error: {}", root.get("Error Message").asText());
                return Optional.empty();
            }
            
            // Check for rate limit
            if (root.has("Note")) {
                log.warn("AlphaVantage API rate limit: {}", root.get("Note").asText());
                return Optional.empty();
            }
            
            // Parse global quote
            JsonNode globalQuote = root.get("Global Quote");
            if (globalQuote != null && globalQuote.has("05. price")) {
                String priceStr = globalQuote.get("05. price").asText();
                return Optional.of(new BigDecimal(priceStr));
            } else {
                log.warn("Invalid response format from AlphaVantage API");
                return Optional.empty();
            }
            
        } catch (Exception e) {
            log.error("Error parsing AlphaVantage response", e);
            return Optional.empty();
        }
    }
}