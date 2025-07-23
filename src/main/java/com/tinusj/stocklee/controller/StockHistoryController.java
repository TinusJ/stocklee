package com.tinusj.stocklee.controller;

import com.tinusj.stocklee.entity.StockHistory;
import com.tinusj.stocklee.service.StockHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for StockHistory entity operations.
 */
@RestController
@RequestMapping("/api/stock-histories")
@RequiredArgsConstructor
public class StockHistoryController {

    private final StockHistoryService stockHistoryService;

    /**
     * Get all stock histories.
     */
    @GetMapping
    public ResponseEntity<List<StockHistory>> getAllStockHistories() {
        List<StockHistory> stockHistories = stockHistoryService.findAll();
        return ResponseEntity.ok(stockHistories);
    }

    /**
     * Get stock history by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<StockHistory> getStockHistoryById(@PathVariable UUID id) {
        return stockHistoryService.findById(id)
                .map(stockHistory -> ResponseEntity.ok(stockHistory))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new stock history.
     */
    @PostMapping
    public ResponseEntity<StockHistory> createStockHistory(@RequestBody StockHistory stockHistory) {
        StockHistory savedStockHistory = stockHistoryService.save(stockHistory);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedStockHistory);
    }

    /**
     * Update an existing stock history.
     */
    @PutMapping("/{id}")
    public ResponseEntity<StockHistory> updateStockHistory(@PathVariable UUID id, @RequestBody StockHistory stockHistory) {
        if (!stockHistoryService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        stockHistory.setId(id);
        StockHistory updatedStockHistory = stockHistoryService.save(stockHistory);
        return ResponseEntity.ok(updatedStockHistory);
    }

    /**
     * Delete stock history by ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStockHistory(@PathVariable UUID id) {
        if (!stockHistoryService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        stockHistoryService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get count of all stock histories.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> getStockHistoryCount() {
        long count = stockHistoryService.count();
        return ResponseEntity.ok(count);
    }
}