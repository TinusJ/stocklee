package com.tinusj.stocklee.service;

import com.tinusj.stocklee.entity.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortfolioUpdateSchedulerTest {

    @Mock
    private StockService stockService;

    @Mock
    private CompositeStockPriceProvider compositeStockPriceProvider;

    @InjectMocks
    private PortfolioUpdateScheduler portfolioUpdateScheduler;

    private Stock testStock;

    @BeforeEach
    void setUp() {
        testStock = new Stock();
        testStock.setSymbol("AAPL");
        testStock.setName("Apple Inc.");
        testStock.setCurrentPrice(new BigDecimal("150.00"));
        testStock.setMarket(Stock.MarketType.NASDAQ);
    }

    @Test
    void testUpdatePortfolio_Success() {
        // Arrange
        List<Stock> stocks = Arrays.asList(testStock);
        BigDecimal newPrice = new BigDecimal("155.00");
        
        when(stockService.findAll()).thenReturn(stocks);
        when(compositeStockPriceProvider.getPrice("AAPL")).thenReturn(Optional.of(newPrice));
        when(stockService.save(any(Stock.class))).thenReturn(testStock);

        // Act
        portfolioUpdateScheduler.updatePortfolio();

        // Assert
        verify(stockService).findAll();
        verify(compositeStockPriceProvider).getPrice("AAPL");
        verify(stockService).save(testStock);
        
        // Verify price updates
        assert testStock.getPreviousPrice().equals(new BigDecimal("150.00"));
        assert testStock.getCurrentPrice().equals(new BigDecimal("155.00"));
    }

    @Test
    void testUpdatePortfolio_ApiFailure() {
        // Arrange
        List<Stock> stocks = Arrays.asList(testStock);
        
        when(stockService.findAll()).thenReturn(stocks);
        when(compositeStockPriceProvider.getPrice("AAPL")).thenReturn(Optional.empty());

        // Act
        portfolioUpdateScheduler.updatePortfolio();

        // Assert
        verify(stockService).findAll();
        verify(compositeStockPriceProvider).getPrice("AAPL");
        verify(stockService, never()).save(any(Stock.class));
        
        // Verify price remains unchanged
        assert testStock.getCurrentPrice().equals(new BigDecimal("150.00"));
        assert testStock.getPreviousPrice() == null;
    }

    @Test
    void testUpdatePortfolio_Exception() {
        // Arrange
        List<Stock> stocks = Arrays.asList(testStock);
        
        when(stockService.findAll()).thenReturn(stocks);
        when(compositeStockPriceProvider.getPrice("AAPL")).thenThrow(new RuntimeException("API Error"));

        // Act
        portfolioUpdateScheduler.updatePortfolio();

        // Assert
        verify(stockService).findAll();
        verify(compositeStockPriceProvider).getPrice("AAPL");
        verify(stockService, never()).save(any(Stock.class));
    }
}