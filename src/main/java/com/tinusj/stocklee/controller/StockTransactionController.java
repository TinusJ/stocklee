package com.tinusj.stocklee.controller;

import com.tinusj.stocklee.entity.StockTransaction;
import com.tinusj.stocklee.service.StockTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for StockTransaction entity operations.
 */
@RestController
@RequestMapping("/api/stock-transactions")
@RequiredArgsConstructor
public class StockTransactionController {

    private final StockTransactionService stockTransactionService;

    /**
     * Get all stock transactions.
     */
    @GetMapping
    public ResponseEntity<List<StockTransaction>> getAllStockTransactions() {
        List<StockTransaction> transactions = stockTransactionService.findAll();
        return ResponseEntity.ok(transactions);
    }

    /**
     * Get stock transaction by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<StockTransaction> getStockTransactionById(@PathVariable UUID id) {
        return stockTransactionService.findById(id)
                .map(transaction -> ResponseEntity.ok(transaction))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new stock transaction.
     */
    @PostMapping
    public ResponseEntity<StockTransaction> createStockTransaction(@RequestBody StockTransaction stockTransaction) {
        StockTransaction savedTransaction = stockTransactionService.save(stockTransaction);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedTransaction);
    }

    /**
     * Update an existing stock transaction.
     */
    @PutMapping("/{id}")
    public ResponseEntity<StockTransaction> updateStockTransaction(@PathVariable UUID id, @RequestBody StockTransaction stockTransaction) {
        if (!stockTransactionService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        stockTransaction.setId(id);
        StockTransaction updatedTransaction = stockTransactionService.save(stockTransaction);
        return ResponseEntity.ok(updatedTransaction);
    }

    /**
     * Delete stock transaction by ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStockTransaction(@PathVariable UUID id) {
        if (!stockTransactionService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        stockTransactionService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get count of all stock transactions.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> getStockTransactionCount() {
        long count = stockTransactionService.count();
        return ResponseEntity.ok(count);
    }
}