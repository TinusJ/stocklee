package com.tinusj.stocklee.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for NasdaqApiService.
 */
@ExtendWith(MockitoExtension.class)
class NasdaqApiServiceTest {

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private NasdaqApiService nasdaqApiService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(nasdaqApiService, "apiKey", "test-key");
        ReflectionTestUtils.setField(nasdaqApiService, "baseUrl", "https://test-api.nasdaq.com");
    }

    @Test
    void testGetPrice_ValidSymbol_ReturnsMockPrice() {
        // Given
        String symbol = "AAPL";

        // When
        Optional<BigDecimal> result = nasdaqApiService.getPrice(symbol);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    void testGetPrice_InvalidSymbol_ReturnsEmpty() {
        // Given
        String invalidSymbol = "INVALID123";

        // When
        Optional<BigDecimal> result = nasdaqApiService.getPrice(invalidSymbol);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void testGetPrice_NullSymbol_ReturnsEmpty() {
        // When
        Optional<BigDecimal> result = nasdaqApiService.getPrice(null);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void testIsValidSymbol_ValidSymbols_ReturnsTrue() {
        // Test various valid symbols
        assertThat(nasdaqApiService.isValidSymbol("AAPL")).isTrue();
        assertThat(nasdaqApiService.isValidSymbol("MSFT")).isTrue();
        assertThat(nasdaqApiService.isValidSymbol("A")).isTrue();
        assertThat(nasdaqApiService.isValidSymbol("GOOGL")).isTrue();
    }

    @Test
    void testIsValidSymbol_InvalidSymbols_ReturnsFalse() {
        // Test various invalid symbols
        assertThat(nasdaqApiService.isValidSymbol(null)).isFalse();
        assertThat(nasdaqApiService.isValidSymbol("")).isFalse();
        assertThat(nasdaqApiService.isValidSymbol("   ")).isFalse();
        assertThat(nasdaqApiService.isValidSymbol("TOOLONG")).isFalse();
        assertThat(nasdaqApiService.isValidSymbol("123")).isFalse();
        assertThat(nasdaqApiService.isValidSymbol("AAPL123")).isFalse();
    }

    @Test
    void testGetHistoricalData_ValidParameters_ReturnsData() {
        // Given
        String symbol = "AAPL";
        LocalDate fromDate = LocalDate.now().minusDays(5);
        LocalDate toDate = LocalDate.now();

        // When
        List<NasdaqApiService.HistoricalStockData> result = 
            nasdaqApiService.getHistoricalData(symbol, fromDate, toDate);

        // Then
        assertThat(result).isNotNull();
        // Should contain data only for weekdays
        assertThat(result.size()).isLessThanOrEqualTo(5);
        
        // Verify data structure
        for (NasdaqApiService.HistoricalStockData data : result) {
            assertThat(data.getSymbol()).isEqualTo(symbol);
            assertThat(data.getDate()).isAfterOrEqualTo(fromDate);
            assertThat(data.getDate()).isBeforeOrEqualTo(toDate);
            assertThat(data.getOpenPrice()).isGreaterThan(BigDecimal.ZERO);
            assertThat(data.getHighPrice()).isGreaterThanOrEqualTo(data.getOpenPrice());
            assertThat(data.getLowPrice()).isLessThanOrEqualTo(data.getOpenPrice());
            assertThat(data.getClosePrice()).isGreaterThan(BigDecimal.ZERO);
            assertThat(data.getVolume()).isGreaterThan(0L);
        }
    }

    @Test
    void testGetHistoricalData_InvalidSymbol_ReturnsEmptyList() {
        // Given
        String invalidSymbol = "INVALID123";
        LocalDate fromDate = LocalDate.now().minusDays(5);
        LocalDate toDate = LocalDate.now();

        // When
        List<NasdaqApiService.HistoricalStockData> result = 
            nasdaqApiService.getHistoricalData(invalidSymbol, fromDate, toDate);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void testGetHistoricalData_WeekendDatesFiltered() {
        // Given
        String symbol = "AAPL";
        // Create a date range that includes a weekend
        LocalDate friday = LocalDate.of(2024, 1, 5); // Friday
        LocalDate tuesday = LocalDate.of(2024, 1, 9); // Tuesday

        // When
        List<NasdaqApiService.HistoricalStockData> result = 
            nasdaqApiService.getHistoricalData(symbol, friday, tuesday);

        // Then
        assertThat(result).isNotNull();
        // Should only contain weekdays: Friday (5th), Monday (8th), Tuesday (9th)
        assertThat(result.size()).isEqualTo(3);
        
        // Verify no weekend dates are included
        for (NasdaqApiService.HistoricalStockData data : result) {
            int dayOfWeek = data.getDate().getDayOfWeek().getValue();
            assertThat(dayOfWeek).isLessThanOrEqualTo(5); // Monday=1, Friday=5
        }
    }
}