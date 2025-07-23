package com.tinusj.stocklee.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinusj.stocklee.entity.Stock;
import com.tinusj.stocklee.repository.StockRepository;
import org.junit.jupiter.api.BeforeEach;
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
 * Integration tests for StockController.
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebMvc
@Transactional
class StockControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StockRepository stockRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void testGetAllStocks_WhenNoStocks_ReturnsEmptyArray() throws Exception {
        mockMvc.perform(get("/api/stocks"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testCreateStock() throws Exception {
        // Given
        Stock stock = new Stock();
        stock.setSymbol("AAPL");
        stock.setName("Apple Inc.");
        stock.setCurrentPrice(new BigDecimal("150.00"));
        stock.setMarket(Stock.MarketType.NASDAQ);
        stock.setDescription("Technology company");

        // When & Then
        mockMvc.perform(post("/api/stocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stock)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.symbol").value("AAPL"))
                .andExpect(jsonPath("$.name").value("Apple Inc."))
                .andExpect(jsonPath("$.currentPrice").value(150.00))
                .andExpect(jsonPath("$.market").value("NASDAQ"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void testGetStockById_WhenExists() throws Exception {
        // Given
        Stock stock = new Stock();
        stock.setSymbol("MSFT");
        stock.setName("Microsoft Corporation");
        stock.setCurrentPrice(new BigDecimal("300.00"));
        stock.setMarket(Stock.MarketType.NASDAQ);

        Stock savedStock = stockRepository.save(stock);

        // When & Then
        mockMvc.perform(get("/api/stocks/{id}", savedStock.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.symbol").value("MSFT"))
                .andExpect(jsonPath("$.name").value("Microsoft Corporation"))
                .andExpect(jsonPath("$.currentPrice").value(300.00));
    }

    @Test
    void testGetStockById_WhenNotExists() throws Exception {
        mockMvc.perform(get("/api/stocks/{id}", "00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetStockCount() throws Exception {
        mockMvc.perform(get("/api/stocks/count"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(0));
    }
}