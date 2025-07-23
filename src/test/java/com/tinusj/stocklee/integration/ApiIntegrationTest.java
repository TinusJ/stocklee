package com.tinusj.stocklee.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinusj.stocklee.entity.Stock;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full integration test demonstrating all API endpoints.
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebMvc
@Transactional
class ApiIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testStockApiEndpoints() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Test GET all stocks (empty)
        mockMvc.perform(get("/api/stocks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        // Test GET count (0)
        mockMvc.perform(get("/api/stocks/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(0));

        // Test POST create stock
        Stock stock = new Stock();
        stock.setSymbol("AAPL");
        stock.setName("Apple Inc.");
        stock.setCurrentPrice(new BigDecimal("150.00"));
        stock.setMarket(Stock.MarketType.NASDAQ);
        stock.setDescription("Technology company");

        String stockResponse = mockMvc.perform(post("/api/stocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stock)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.symbol").value("AAPL"))
                .andExpect(jsonPath("$.name").value("Apple Inc."))
                .andExpect(jsonPath("$.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Stock createdStock = objectMapper.readValue(stockResponse, Stock.class);

        // Test GET by ID
        mockMvc.perform(get("/api/stocks/{id}", createdStock.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("AAPL"));

        // Test PUT update
        stock.setName("Apple Inc. - Updated");
        mockMvc.perform(put("/api/stocks/{id}", createdStock.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stock)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Apple Inc. - Updated"));

        // Test GET count (1)
        mockMvc.perform(get("/api/stocks/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(1));

        // Test DELETE
        mockMvc.perform(delete("/api/stocks/{id}", createdStock.getId()))
                .andExpect(status().isNoContent());

        // Test GET by ID after delete (404)
        mockMvc.perform(get("/api/stocks/{id}", createdStock.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testAllControllerEndpointsExist() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Test that all controller endpoints are accessible
        String[] endpoints = {
                "/api/stocks",
                "/api/stock-transactions", 
                "/api/user-profiles",
                "/api/owned-stocks",
                "/api/history-logs",
                "/api/stock-histories",
                "/api/transaction-histories"
        };

        for (String endpoint : endpoints) {
            mockMvc.perform(get(endpoint))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());

            mockMvc.perform(get(endpoint + "/count"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isNumber());
        }
    }
}