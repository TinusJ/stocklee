package com.tinusj.stocklee.controller;

import com.tinusj.stocklee.dto.HistoricalStockDataDto;
import com.tinusj.stocklee.entity.Stock;
import com.tinusj.stocklee.entity.StockHistory;
import com.tinusj.stocklee.service.StockHistoryService;
import com.tinusj.stocklee.service.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for StockController historical data endpoints.
 */
@ExtendWith(MockitoExtension.class)
class StockControllerHistoricalTest {

    @Mock
    private StockService stockService;

    @Mock
    private StockHistoryService stockHistoryService;

    @InjectMocks
    private StockController stockController;

    private Stock testStock;
    private List<StockHistory> testHistoricalData;

    @BeforeEach
    void setUp() {
        testStock = new Stock();
        testStock.setSymbol("AAPL");
        testStock.setName("Apple Inc.");
        testStock.setCurrentPrice(new BigDecimal("150.00"));
        testStock.setMarket(Stock.MarketType.NASDAQ);

        testHistoricalData = Arrays.asList(
            createStockHistory(testStock, LocalDate.now().minusDays(1)),
            createStockHistory(testStock, LocalDate.now().minusDays(2))
        );
    }

    @Test
    void testGetHistoricalData_ValidSymbolAndDateRange_ReturnsData() {
        // Given
        String symbol = "AAPL";
        LocalDate fromDate = LocalDate.now().minusDays(5);
        LocalDate toDate = LocalDate.now();

        when(stockHistoryService.getHistoricalData(symbol, fromDate, toDate))
            .thenReturn(testHistoricalData);

        // When
        ResponseEntity<List<HistoricalStockDataDto>> response = 
            stockController.getHistoricalData(symbol, fromDate, toDate);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        
        HistoricalStockDataDto firstDto = response.getBody().get(0);
        assertThat(firstDto.getSymbol()).isEqualTo("AAPL");
        assertThat(firstDto.getOpenPrice()).isEqualTo(new BigDecimal("145.00"));
        assertThat(firstDto.getClosePrice()).isEqualTo(new BigDecimal("149.00"));
        
        verify(stockHistoryService).getHistoricalData(symbol, fromDate, toDate);
    }

    @Test
    void testGetHistoricalDataLastDays_ValidParameters_ReturnsData() {
        // Given
        String symbol = "AAPL";
        int days = 5;
        
        when(stockHistoryService.getHistoricalData(eq(symbol), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(testHistoricalData);

        // When
        ResponseEntity<List<HistoricalStockDataDto>> response = 
            stockController.getHistoricalDataLastDays(symbol, days);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        
        verify(stockHistoryService).getHistoricalData(eq(symbol), any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    void testFetchHistoricalData_ExistingStock_InitiatesFetch() {
        // Given
        String symbol = "AAPL";
        LocalDate fromDate = LocalDate.now().minusDays(30);
        LocalDate toDate = LocalDate.now();

        when(stockService.findBySymbol(symbol)).thenReturn(Optional.of(testStock));

        // When
        ResponseEntity<String> response = 
            stockController.fetchHistoricalData(symbol, fromDate, toDate);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Historical data fetch initiated for AAPL");
        
        verify(stockService).findBySymbol(symbol);
        verify(stockHistoryService).fetchAndStoreHistoricalData(testStock, fromDate, toDate);
    }

    @Test
    void testFetchHistoricalData_NonExistentStock_ReturnsNotFound() {
        // Given
        String symbol = "NONEXISTENT";

        when(stockService.findBySymbol(symbol)).thenReturn(Optional.empty());

        // When
        ResponseEntity<String> response = 
            stockController.fetchHistoricalData(symbol, null, null);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        
        verify(stockService).findBySymbol(symbol);
        verify(stockHistoryService, never()).fetchAndStoreHistoricalData(any(), any(), any());
    }

    @Test
    void testFetchHistoricalData_NoDateParameters_UsesDefaults() {
        // Given
        String symbol = "AAPL";

        when(stockService.findBySymbol(symbol)).thenReturn(Optional.of(testStock));

        // When
        ResponseEntity<String> response = 
            stockController.fetchHistoricalData(symbol, null, null);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        verify(stockService).findBySymbol(symbol);
        verify(stockHistoryService).fetchAndStoreHistoricalData(eq(testStock), any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    void testGetLatestStockData_ValidSymbol_ReturnsCurrentPrice() {
        // Given
        String symbol = "AAPL";
        
        when(stockService.findBySymbol(symbol)).thenReturn(Optional.of(testStock));

        // When
        ResponseEntity<?> response = stockController.getLatestStockData(symbol);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(stockService).findBySymbol(symbol);
    }

    private StockHistory createStockHistory(Stock stock, LocalDate date) {
        StockHistory history = new StockHistory(
            stock,
            date,
            new BigDecimal("145.00"),
            new BigDecimal("150.00"),
            new BigDecimal("144.00"),
            new BigDecimal("149.00"),
            1000000L
        );
        return history;
    }
}