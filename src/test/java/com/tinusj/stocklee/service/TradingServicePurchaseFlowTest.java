package com.tinusj.stocklee.service;

import com.tinusj.stocklee.dto.BuyStockDto;
import com.tinusj.stocklee.entity.Stock;
import com.tinusj.stocklee.entity.UserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradingServicePurchaseFlowTest {

    @Mock
    private StockService stockService;
    
    @Mock
    private OwnedStockService ownedStockService;
    
    @Mock
    private UserProfileService userProfileService;
    
    @Mock
    private StockTransactionService stockTransactionService;
    
    @Mock
    private HistoryLogService historyLogService;
    
    @Mock
    private CompositeStockPriceProvider compositeStockPriceProvider;

    @InjectMocks
    private TradingService tradingService;

    private UserProfile testUser;
    private Stock testStock;
    private BuyStockDto buyStockDto;

    @BeforeEach
    void setUp() {
        testUser = new UserProfile();
        testUser.setUsername("testuser");
        testUser.setBalance(new BigDecimal("1000.00"));

        testStock = new Stock();
        testStock.setSymbol("AAPL");
        testStock.setName("Apple Inc.");
        testStock.setCurrentPrice(new BigDecimal("150.00"));
        testStock.setMarket(Stock.MarketType.NASDAQ);

        buyStockDto = new BuyStockDto();
        buyStockDto.setSymbol("AAPL");
        buyStockDto.setInvestmentAmount(new BigDecimal("300.00"));
    }

    @Test
    void testBuyStock_WithCurrentPrice() {
        // Arrange
        when(stockService.findBySymbol("AAPL")).thenReturn(Optional.of(testStock));
        when(userProfileService.save(any(UserProfile.class))).thenReturn(testUser);
        when(stockTransactionService.save(any())).thenReturn(null);
        when(historyLogService.save(any())).thenReturn(null);

        // Act
        String result = tradingService.buyStock(testUser, buyStockDto);

        // Assert
        assertTrue(result.contains("Successfully bought"));
        verify(stockService).findBySymbol("AAPL");
        verify(ownedStockService).addShares(eq(testUser), eq(testStock), any(BigDecimal.class), eq(new BigDecimal("150.00")));
    }

    @Test
    void testBuyStock_WithPriceOverride() {
        // Arrange
        BigDecimal overridePrice = new BigDecimal("155.00");
        buyStockDto.setPurchasePrice(overridePrice);
        
        when(stockService.findBySymbol("AAPL")).thenReturn(Optional.of(testStock));
        when(userProfileService.save(any(UserProfile.class))).thenReturn(testUser);
        when(stockTransactionService.save(any())).thenReturn(null);
        when(historyLogService.save(any())).thenReturn(null);

        // Act
        String result = tradingService.buyStock(testUser, buyStockDto);

        // Assert
        assertTrue(result.contains("Successfully bought"));
        verify(stockService).findBySymbol("AAPL");
        verify(ownedStockService).addShares(eq(testUser), eq(testStock), any(BigDecimal.class), eq(overridePrice));
    }

    @Test
    void testBuyStock_InsufficientBalance() {
        // Arrange
        testUser.setBalance(new BigDecimal("100.00")); // Less than investment amount

        // Act
        String result = tradingService.buyStock(testUser, buyStockDto);

        // Assert
        assertTrue(result.contains("Insufficient balance"));
        verify(stockService, never()).findBySymbol(any());
        verify(ownedStockService, never()).addShares(any(), any(), any(), any());
    }
}