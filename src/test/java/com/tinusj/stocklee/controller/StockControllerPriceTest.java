package com.tinusj.stocklee.controller;

import com.tinusj.stocklee.service.StockService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StockController.class)
@ExtendWith(MockitoExtension.class)
class StockControllerPriceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StockService stockService;

    @Test
    void testGetCurrentPrice_Success() throws Exception {
        // Arrange
        BigDecimal price = new BigDecimal("150.00");
        when(stockService.getCurrentPrice("AAPL")).thenReturn(Optional.of(price));

        // Act & Assert
        mockMvc.perform(get("/api/stocks/price/AAPL"))
                .andExpect(status().isOk())
                .andExpect(content().string("150.00"));
    }

    @Test
    void testGetCurrentPrice_NotFound() throws Exception {
        // Arrange
        when(stockService.getCurrentPrice("INVALID")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/stocks/price/INVALID"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetCurrentPrice_CaseInsensitive() throws Exception {
        // Arrange
        BigDecimal price = new BigDecimal("150.00");
        when(stockService.getCurrentPrice("AAPL")).thenReturn(Optional.of(price));

        // Act & Assert
        mockMvc.perform(get("/api/stocks/price/aapl"))
                .andExpect(status().isOk())
                .andExpect(content().string("150.00"));
    }
}