package com.tinusj.stocklee.service;

import com.tinusj.stocklee.entity.Stock;
import com.tinusj.stocklee.entity.StockHistory;
import com.tinusj.stocklee.repository.StockHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for enhanced StockHistoryService.
 */
@ExtendWith(MockitoExtension.class)
class StockHistoryServiceTest {

    @Mock
    private StockHistoryRepository stockHistoryRepository;

    @Mock
    private NasdaqApiService nasdaqApiService;

    @InjectMocks
    private StockHistoryService stockHistoryService;

    private Stock testStock;
    private LocalDate fromDate;
    private LocalDate toDate;

    @BeforeEach
    void setUp() {
        testStock = new Stock();
        testStock.setSymbol("AAPL");
        testStock.setName("Apple Inc.");
        testStock.setCurrentPrice(new BigDecimal("150.00"));
        testStock.setMarket(Stock.MarketType.NASDAQ);

        fromDate = LocalDate.now().minusDays(5);
        toDate = LocalDate.now();
        
        // Inject the mocked NasdaqApiService
        ReflectionTestUtils.setField(stockHistoryService, "nasdaqApiService", nasdaqApiService);
    }

    @Test
    void testGetHistoricalDataBySymbol() {
        // Given
        String symbol = "AAPL";
        List<StockHistory> expectedHistory = Arrays.asList(
            createStockHistory(testStock, LocalDate.now().minusDays(1)),
            createStockHistory(testStock, LocalDate.now().minusDays(2))
        );

        when(stockHistoryRepository.findByStockSymbolAndDateBetween(symbol, fromDate, toDate))
            .thenReturn(expectedHistory);

        // When
        List<StockHistory> result = stockHistoryService.getHistoricalData(symbol, fromDate, toDate);

        // Then
        assertThat(result).isEqualTo(expectedHistory);
        verify(stockHistoryRepository).findByStockSymbolAndDateBetween(symbol, fromDate, toDate);
    }

    @Test
    void testGetHistoricalDataByStock() {
        // Given
        List<StockHistory> expectedHistory = Arrays.asList(
            createStockHistory(testStock, LocalDate.now().minusDays(1)),
            createStockHistory(testStock, LocalDate.now().minusDays(2))
        );

        when(stockHistoryRepository.findByStockAndDateBetween(testStock, fromDate, toDate))
            .thenReturn(expectedHistory);

        // When
        List<StockHistory> result = stockHistoryService.getHistoricalData(testStock, fromDate, toDate);

        // Then
        assertThat(result).isEqualTo(expectedHistory);
        verify(stockHistoryRepository).findByStockAndDateBetween(testStock, fromDate, toDate);
    }

    @Test
    void testFetchAndStoreHistoricalData_NewData() {
        // Given
        List<NasdaqApiService.HistoricalStockData> apiData = Arrays.asList(
            new NasdaqApiService.HistoricalStockData(
                "AAPL", 
                LocalDate.now().minusDays(1),
                new BigDecimal("145.00"),
                new BigDecimal("150.00"),
                new BigDecimal("144.00"),
                new BigDecimal("149.00"),
                1000000L
            ),
            new NasdaqApiService.HistoricalStockData(
                "AAPL",
                LocalDate.now().minusDays(2),
                new BigDecimal("142.00"),
                new BigDecimal("147.00"),
                new BigDecimal("141.00"),
                new BigDecimal("145.00"),
                1200000L
            )
        );

        when(nasdaqApiService.getHistoricalData("AAPL", fromDate, toDate))
            .thenReturn(apiData);
        when(stockHistoryRepository.findByStockAndDate(eq(testStock), any(LocalDate.class)))
            .thenReturn(Arrays.asList()); // No existing data
        when(stockHistoryRepository.save(any(StockHistory.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        stockHistoryService.fetchAndStoreHistoricalData(testStock, fromDate, toDate);

        // Then
        verify(nasdaqApiService).getHistoricalData("AAPL", fromDate, toDate);
        verify(stockHistoryRepository, times(2)).save(any(StockHistory.class));
    }

    @Test
    void testFetchAndStoreHistoricalData_ExistingData() {
        // Given
        List<NasdaqApiService.HistoricalStockData> apiData = Arrays.asList(
            new NasdaqApiService.HistoricalStockData(
                "AAPL",
                LocalDate.now().minusDays(1),
                new BigDecimal("145.00"),
                new BigDecimal("150.00"),
                new BigDecimal("144.00"),
                new BigDecimal("149.00"),
                1000000L
            )
        );

        StockHistory existingHistory = createStockHistory(testStock, LocalDate.now().minusDays(1));

        when(nasdaqApiService.getHistoricalData("AAPL", fromDate, toDate))
            .thenReturn(apiData);
        when(stockHistoryRepository.findByStockAndDate(eq(testStock), any(LocalDate.class)))
            .thenReturn(Arrays.asList(existingHistory)); // Existing data found

        // When
        stockHistoryService.fetchAndStoreHistoricalData(testStock, fromDate, toDate);

        // Then
        verify(nasdaqApiService).getHistoricalData("AAPL", fromDate, toDate);
        verify(stockHistoryRepository, never()).save(any(StockHistory.class)); // Should not save if data exists
    }

    @Test
    void testFetchAndStoreHistoricalData_ApiError() {
        // Given
        when(nasdaqApiService.getHistoricalData("AAPL", fromDate, toDate))
            .thenThrow(new RuntimeException("API Error"));

        // When
        stockHistoryService.fetchAndStoreHistoricalData(testStock, fromDate, toDate);

        // Then
        verify(nasdaqApiService).getHistoricalData("AAPL", fromDate, toDate);
        verify(stockHistoryRepository, never()).save(any(StockHistory.class));
        // Should not throw exception, just log the error
    }

    @Test
    void testFetchAndStoreHistoricalData_NasdaqServiceUnavailable() {
        // Given - NasdaqApiService is null
        ReflectionTestUtils.setField(stockHistoryService, "nasdaqApiService", null);

        // When
        stockHistoryService.fetchAndStoreHistoricalData(testStock, fromDate, toDate);

        // Then
        verify(stockHistoryRepository, never()).save(any(StockHistory.class));
        // Should handle gracefully when service is not available
    }

    private StockHistory createStockHistory(Stock stock, LocalDate date) {
        return new StockHistory(
            stock,
            date,
            new BigDecimal("145.00"),
            new BigDecimal("150.00"),
            new BigDecimal("144.00"),
            new BigDecimal("149.00"),
            1000000L
        );
    }
}