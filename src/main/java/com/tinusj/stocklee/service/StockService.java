package com.tinusj.stocklee.service;

import com.tinusj.stocklee.entity.Stock;
import com.tinusj.stocklee.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service layer for Stock entity operations.
 */
@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final CompositeStockPriceProvider compositeStockPriceProvider;

    /**
     * Find all stocks.
     */
    public List<Stock> findAll() {
        return stockRepository.findAll();
    }

    /**
     * Find stock by ID.
     */
    public Optional<Stock> findById(UUID id) {
        return stockRepository.findById(id);
    }

    /**
     * Find stock by symbol.
     */
    public Optional<Stock> findBySymbol(String symbol) {
        return stockRepository.findBySymbol(symbol);
    }

    /**
     * Check if stock exists by symbol.
     */
    public boolean existsBySymbol(String symbol) {
        return stockRepository.existsBySymbol(symbol);
    }

    /**
     * Save or update a stock.
     */
    public Stock save(Stock stock) {
        return stockRepository.save(stock);
    }

    /**
     * Delete stock by ID.
     */
    public void deleteById(UUID id) {
        stockRepository.deleteById(id);
    }

    /**
     * Check if stock exists by ID.
     */
    public boolean existsById(UUID id) {
        return stockRepository.existsById(id);
    }

    /**
     * Get count of all stocks.
     */
    public long count() {
        return stockRepository.count();
    }

    /**
     * Get current price for a stock symbol. 
     * Returns the stored price if stock exists, otherwise fetches from API.
     */
    public Optional<BigDecimal> getCurrentPrice(String symbol) {
        Optional<Stock> stockOpt = findBySymbol(symbol);
        if (stockOpt.isPresent()) {
            return Optional.of(stockOpt.get().getCurrentPrice());
        }
        
        // If stock doesn't exist, fetch current price from API
        return compositeStockPriceProvider.getPrice(symbol);
    }
}