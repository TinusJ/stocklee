package com.tinusj.stocklee.entity;

import com.tinusj.stocklee.entity.enums.MarketType;
import com.tinusj.stocklee.entity.enums.TransactionStatus;
import com.tinusj.stocklee.entity.enums.TransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for entity relationships and database mappings
 */
@DataJpaTest
@DirtiesContext
public class EntityRelationshipTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void testStockEntityCreation() {
        // Given
        Stock stock = new Stock();
        stock.setSymbol("AAPL");
        stock.setName("Apple Inc.");
        stock.setCurrentPrice(new BigDecimal("150.00"));
        stock.setDescription("Apple Inc. is a technology company.");
        stock.setMarket(MarketType.NASDAQ);

        // When
        Stock savedStock = entityManager.persistAndFlush(stock);

        // Then
        assertThat(savedStock.getId()).isNotNull();
        assertThat(savedStock.getSymbol()).isEqualTo("AAPL");
        assertThat(savedStock.getName()).isEqualTo("Apple Inc.");
        assertThat(savedStock.getCurrentPrice()).isEqualTo(new BigDecimal("150.00"));
        assertThat(savedStock.getMarket()).isEqualTo(MarketType.NASDAQ);
        assertThat(savedStock.getCreatedAt()).isNotNull();
        assertThat(savedStock.getUpdatedAt()).isNotNull();
    }

    @Test
    public void testUserProfileEntityCreation() {
        // Given
        UserProfile userProfile = new UserProfile();
        userProfile.setUsername("testuser");
        userProfile.setEmail("test@example.com");
        userProfile.setBalance(new BigDecimal("1000.00"));

        // When
        UserProfile savedUserProfile = entityManager.persistAndFlush(userProfile);

        // Then
        assertThat(savedUserProfile.getId()).isNotNull();
        assertThat(savedUserProfile.getUsername()).isEqualTo("testuser");
        assertThat(savedUserProfile.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUserProfile.getBalance()).isEqualTo(new BigDecimal("1000.00"));
        assertThat(savedUserProfile.getCreatedAt()).isNotNull();
        assertThat(savedUserProfile.getUpdatedAt()).isNotNull();
    }

    @Test
    public void testStockTransactionEntityCreation() {
        // Given - Create stock and user first
        Stock stock = new Stock();
        stock.setSymbol("TSLA");
        stock.setName("Tesla Inc.");
        stock.setCurrentPrice(new BigDecimal("250.00"));
        stock.setMarket(MarketType.NASDAQ);
        Stock savedStock = entityManager.persistAndFlush(stock);

        UserProfile userProfile = new UserProfile();
        userProfile.setUsername("trader");
        userProfile.setEmail("trader@example.com");
        userProfile.setBalance(new BigDecimal("5000.00"));
        UserProfile savedUserProfile = entityManager.persistAndFlush(userProfile);

        // Create transaction
        StockTransaction transaction = new StockTransaction();
        transaction.setTransactionType(TransactionType.BUY);
        transaction.setQuantity(10);
        transaction.setPrice(new BigDecimal("250.00"));
        transaction.setStock(savedStock);
        transaction.setUserProfile(savedUserProfile);

        // When
        StockTransaction savedTransaction = entityManager.persistAndFlush(transaction);

        // Then
        assertThat(savedTransaction.getId()).isNotNull();
        assertThat(savedTransaction.getTransactionType()).isEqualTo(TransactionType.BUY);
        assertThat(savedTransaction.getQuantity()).isEqualTo(10);
        assertThat(savedTransaction.getPrice()).isEqualTo(new BigDecimal("250.00"));
        assertThat(savedTransaction.getTotalValue()).isEqualTo(new BigDecimal("2500.00"));
        assertThat(savedTransaction.getStock().getSymbol()).isEqualTo("TSLA");
        assertThat(savedTransaction.getUserProfile().getUsername()).isEqualTo("trader");
        assertThat(savedTransaction.getTimestamp()).isNotNull();
    }

    @Test
    public void testOwnedStockEntityCreation() {
        // Given - Create stock and user first
        Stock stock = new Stock();
        stock.setSymbol("GOOGL");
        stock.setName("Alphabet Inc.");
        stock.setCurrentPrice(new BigDecimal("2800.00"));
        stock.setMarket(MarketType.NASDAQ);
        Stock savedStock = entityManager.persistAndFlush(stock);

        UserProfile userProfile = new UserProfile();
        userProfile.setUsername("investor");
        userProfile.setEmail("investor@example.com");
        userProfile.setBalance(new BigDecimal("10000.00"));
        UserProfile savedUserProfile = entityManager.persistAndFlush(userProfile);

        // Create owned stock
        OwnedStock ownedStock = new OwnedStock();
        ownedStock.setQuantity(5);
        ownedStock.setAveragePrice(new BigDecimal("2750.00"));
        ownedStock.setStock(savedStock);
        ownedStock.setUserProfile(savedUserProfile);

        // When
        OwnedStock savedOwnedStock = entityManager.persistAndFlush(ownedStock);

        // Then
        assertThat(savedOwnedStock.getId()).isNotNull();
        assertThat(savedOwnedStock.getQuantity()).isEqualTo(5);
        assertThat(savedOwnedStock.getAveragePrice()).isEqualTo(new BigDecimal("2750.00"));
        assertThat(savedOwnedStock.getTotalValue()).isEqualTo(new BigDecimal("13750.00"));
        assertThat(savedOwnedStock.getStock().getSymbol()).isEqualTo("GOOGL");
        assertThat(savedOwnedStock.getUserProfile().getUsername()).isEqualTo("investor");
    }

    @Test
    public void testHistoryLogEntityCreation() {
        // Given - Create user first
        UserProfile userProfile = new UserProfile();
        userProfile.setUsername("logger");
        userProfile.setEmail("logger@example.com");
        userProfile.setBalance(new BigDecimal("2000.00"));
        UserProfile savedUserProfile = entityManager.persistAndFlush(userProfile);

        // Create history log
        HistoryLog historyLog = new HistoryLog();
        historyLog.setAction("User purchased 10 shares of AAPL");
        historyLog.setUserProfile(savedUserProfile);

        // When
        HistoryLog savedHistoryLog = entityManager.persistAndFlush(historyLog);

        // Then
        assertThat(savedHistoryLog.getId()).isNotNull();
        assertThat(savedHistoryLog.getAction()).isEqualTo("User purchased 10 shares of AAPL");
        assertThat(savedHistoryLog.getUserProfile().getUsername()).isEqualTo("logger");
        assertThat(savedHistoryLog.getTimestamp()).isNotNull();
    }

    @Test
    public void testStockHistoryEntityCreation() {
        // Given - Create stock first
        Stock stock = new Stock();
        stock.setSymbol("MSFT");
        stock.setName("Microsoft Corporation");
        stock.setCurrentPrice(new BigDecimal("300.00"));
        stock.setMarket(MarketType.NASDAQ);
        Stock savedStock = entityManager.persistAndFlush(stock);

        // Create stock history
        StockHistory stockHistory = new StockHistory();
        stockHistory.setPrice(new BigDecimal("295.00"));
        stockHistory.setStock(savedStock);

        // When
        StockHistory savedStockHistory = entityManager.persistAndFlush(stockHistory);

        // Then
        assertThat(savedStockHistory.getId()).isNotNull();
        assertThat(savedStockHistory.getPrice()).isEqualTo(new BigDecimal("295.00"));
        assertThat(savedStockHistory.getStock().getSymbol()).isEqualTo("MSFT");
        assertThat(savedStockHistory.getTimestamp()).isNotNull();
    }

    @Test
    public void testTransactionHistoryEntityCreation() {
        // Given - Create stock, user, and transaction first
        Stock stock = new Stock();
        stock.setSymbol("AMZN");
        stock.setName("Amazon.com Inc.");
        stock.setCurrentPrice(new BigDecimal("3000.00"));
        stock.setMarket(MarketType.NASDAQ);
        Stock savedStock = entityManager.persistAndFlush(stock);

        UserProfile userProfile = new UserProfile();
        userProfile.setUsername("amazontrader");
        userProfile.setEmail("amazontrader@example.com");
        userProfile.setBalance(new BigDecimal("15000.00"));
        UserProfile savedUserProfile = entityManager.persistAndFlush(userProfile);

        StockTransaction transaction = new StockTransaction();
        transaction.setTransactionType(TransactionType.SELL);
        transaction.setQuantity(2);
        transaction.setPrice(new BigDecimal("3000.00"));
        transaction.setStock(savedStock);
        transaction.setUserProfile(savedUserProfile);
        StockTransaction savedTransaction = entityManager.persistAndFlush(transaction);

        // Create transaction history
        TransactionHistory transactionHistory = new TransactionHistory();
        transactionHistory.setStatus(TransactionStatus.COMPLETED);
        transactionHistory.setStockTransaction(savedTransaction);

        // When
        TransactionHistory savedTransactionHistory = entityManager.persistAndFlush(transactionHistory);

        // Then
        assertThat(savedTransactionHistory.getId()).isNotNull();
        assertThat(savedTransactionHistory.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
        assertThat(savedTransactionHistory.getStockTransaction().getTransactionType()).isEqualTo(TransactionType.SELL);
        assertThat(savedTransactionHistory.getTimestamp()).isNotNull();
    }
}