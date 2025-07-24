package com.tinusj.stocklee.controller;

import com.tinusj.stocklee.entity.Stock;
import com.tinusj.stocklee.service.StockService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StockController.class)
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class StockControllerPriceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StockService stockService;

    @Test
    void testGetCurrentPrice_Success() throws Exception {
        // Arrange - Mock existing stock in database
        Stock stock = new Stock();
        stock.setSymbol("AAPL");
        stock.setName("Apple Inc.");
        stock.setCurrentPrice(new BigDecimal("150.00"));
        stock.setPreviousPrice(new BigDecimal("148.00"));
        
        when(stockService.findBySymbol("AAPL")).thenReturn(Optional.of(stock));

        // Act & Assert
        mockMvc.perform(get("/api/stocks/price/AAPL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("AAPL"))
                .andExpect(jsonPath("$.name").value("Apple Inc."))
                .andExpect(jsonPath("$.currentPrice").value(150.00))
                .andExpect(jsonPath("$.previousPrice").value(148.00))
                .andExpect(jsonPath("$.live").value(true));
    }

    @Test
    void testGetCurrentPrice_NotFound() throws Exception {
        // Arrange - Mock no stock found and no API price
        when(stockService.findBySymbol("INVALID")).thenReturn(Optional.empty());
        when(stockService.getCurrentPrice("INVALID")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/stocks/price/INVALID"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetCurrentPrice_CaseInsensitive() throws Exception {
        // Arrange - Mock existing stock in database (should be converted to uppercase)
        Stock stock = new Stock();
        stock.setSymbol("AAPL");
        stock.setName("Apple Inc.");
        stock.setCurrentPrice(new BigDecimal("150.00"));
        stock.setPreviousPrice(new BigDecimal("148.00"));
        
        when(stockService.findBySymbol("AAPL")).thenReturn(Optional.of(stock));

        // Act & Assert
        mockMvc.perform(get("/api/stocks/price/aapl"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("AAPL"))
                .andExpect(jsonPath("$.currentPrice").value(150.00))
                .andExpect(jsonPath("$.live").value(true));
    }

    @Test
    void testGetCurrentPrice_FromAPI() throws Exception {
        // Arrange - Mock no stock in database but price available from API
        when(stockService.findBySymbol("MSFT")).thenReturn(Optional.empty());
        when(stockService.getCurrentPrice("MSFT")).thenReturn(Optional.of(new BigDecimal("300.00")));

        // Act & Assert
        mockMvc.perform(get("/api/stocks/price/MSFT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("MSFT"))
                .andExpect(jsonPath("$.name").value("MSFT"))
                .andExpect(jsonPath("$.currentPrice").value(300.00))
                .andExpect(jsonPath("$.previousPrice").doesNotExist())
                .andExpect(jsonPath("$.live").value(false));
    }
}