package com.tinusj.stocklee.controller;

import com.tinusj.stocklee.dto.CurrentPriceDto;
import com.tinusj.stocklee.entity.Stock;
import com.tinusj.stocklee.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for Stock entity operations.
 */
@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    /**
     * Get all stocks.
     */
    @GetMapping
    public ResponseEntity<List<Stock>> getAllStocks() {
        List<Stock> stocks = stockService.findAll();
        return ResponseEntity.ok(stocks);
    }

    /**
     * Get stock by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Stock> getStockById(@PathVariable UUID id) {
        return stockService.findById(id)
                .map(stock -> ResponseEntity.ok(stock))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new stock.
     */
    @PostMapping
    public ResponseEntity<Stock> createStock(@RequestBody Stock stock) {
        Stock savedStock = stockService.save(stock);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedStock);
    }

    /**
     * Update an existing stock.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Stock> updateStock(@PathVariable UUID id, @RequestBody Stock stock) {
        if (!stockService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        stock.setId(id);
        Stock updatedStock = stockService.save(stock);
        return ResponseEntity.ok(updatedStock);
    }

    /**
     * Delete stock by ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStock(@PathVariable UUID id) {
        if (!stockService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        stockService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get count of all stocks.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> getStockCount() {
        long count = stockService.count();
        return ResponseEntity.ok(count);
    }

    /**
     * Get current price for a stock symbol.
     * Used for prepopulating purchase price in the UI.
     */
    @GetMapping("/price/{symbol}")
    public ResponseEntity<CurrentPriceDto> getCurrentPrice(@PathVariable String symbol) {
        String upperSymbol = symbol.toUpperCase();
        
        // First try to get from database (existing stock)
        var stockOpt = stockService.findBySymbol(upperSymbol);
        if (stockOpt.isPresent()) {
            Stock stock = stockOpt.get();
            CurrentPriceDto dto = new CurrentPriceDto(
                stock.getSymbol(),
                stock.getName(),
                stock.getCurrentPrice(),
                stock.getPreviousPrice(),
                true
            );
            return ResponseEntity.ok(dto);
        }
        
        // If not in database, try to fetch from API
        var priceOpt = stockService.getCurrentPrice(upperSymbol);
        if (priceOpt.isPresent()) {
            CurrentPriceDto dto = new CurrentPriceDto(
                upperSymbol,
                upperSymbol, // We don't have name from API call
                priceOpt.get(),
                null,
                false
            );
            return ResponseEntity.ok(dto);
        }
        
        return ResponseEntity.notFound().build();
    }
}