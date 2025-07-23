package com.tinusj.stocklee.service;

import com.tinusj.stocklee.entity.Stock;
import com.tinusj.stocklee.repository.StockRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for StockService.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class StockServiceTest {

    @Autowired
    private StockService stockService;

    @Autowired
    private StockRepository stockRepository;

    @Test
    void testFindAll_WhenNoStocks_ReturnsEmptyList() {
        List<Stock> stocks = stockService.findAll();
        assertThat(stocks).isEmpty();
    }

    @Test
    void testSaveAndFindById() {
        // Given
        Stock stock = new Stock();
        stock.setSymbol("AAPL");
        stock.setName("Apple Inc.");
        stock.setCurrentPrice(new BigDecimal("150.00"));
        stock.setMarket(Stock.MarketType.NASDAQ);
        stock.setDescription("Technology company");

        // When
        Stock savedStock = stockService.save(stock);

        // Then
        assertThat(savedStock.getId()).isNotNull();
        assertThat(savedStock.getSymbol()).isEqualTo("AAPL");

        Optional<Stock> foundStock = stockService.findById(savedStock.getId());
        assertThat(foundStock).isPresent();
        assertThat(foundStock.get().getSymbol()).isEqualTo("AAPL");
    }

    @Test
    void testCount() {
        // Given
        Stock stock = new Stock();
        stock.setSymbol("MSFT");
        stock.setName("Microsoft Corporation");
        stock.setCurrentPrice(new BigDecimal("300.00"));
        stock.setMarket(Stock.MarketType.NASDAQ);

        // When
        long countBefore = stockService.count();
        stockService.save(stock);
        long countAfter = stockService.count();

        // Then
        assertThat(countAfter).isEqualTo(countBefore + 1);
    }

    @Test
    void testExistsById() {
        // Given
        Stock stock = new Stock();
        stock.setSymbol("GOOGL");
        stock.setName("Alphabet Inc.");
        stock.setCurrentPrice(new BigDecimal("2500.00"));
        stock.setMarket(Stock.MarketType.NASDAQ);

        // When
        Stock savedStock = stockService.save(stock);

        // Then
        assertThat(stockService.existsById(savedStock.getId())).isTrue();
    }

    @Test
    void testDeleteById() {
        // Given
        Stock stock = new Stock();
        stock.setSymbol("TSLA");
        stock.setName("Tesla, Inc.");
        stock.setCurrentPrice(new BigDecimal("800.00"));
        stock.setMarket(Stock.MarketType.NASDAQ);

        Stock savedStock = stockService.save(stock);

        // When
        stockService.deleteById(savedStock.getId());

        // Then
        assertThat(stockService.existsById(savedStock.getId())).isFalse();
        assertThat(stockService.findById(savedStock.getId())).isEmpty();
    }
}