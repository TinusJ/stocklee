package com.tinusj.stocklee.controller;

import com.tinusj.stocklee.entity.TransactionHistory;
import com.tinusj.stocklee.service.TransactionHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for TransactionHistory entity operations.
 */
@RestController
@RequestMapping("/api/transaction-histories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class TransactionHistoryController {

    private final TransactionHistoryService transactionHistoryService;

    /**
     * Get all transaction histories.
     */
    @GetMapping
    public ResponseEntity<List<TransactionHistory>> getAllTransactionHistories() {
        List<TransactionHistory> transactionHistories = transactionHistoryService.findAll();
        return ResponseEntity.ok(transactionHistories);
    }

    /**
     * Get transaction history by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TransactionHistory> getTransactionHistoryById(@PathVariable UUID id) {
        return transactionHistoryService.findById(id)
                .map(transactionHistory -> ResponseEntity.ok(transactionHistory))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new transaction history.
     */
    @PostMapping
    public ResponseEntity<TransactionHistory> createTransactionHistory(@RequestBody TransactionHistory transactionHistory) {
        TransactionHistory savedTransactionHistory = transactionHistoryService.save(transactionHistory);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedTransactionHistory);
    }

    /**
     * Update an existing transaction history.
     */
    @PutMapping("/{id}")
    public ResponseEntity<TransactionHistory> updateTransactionHistory(@PathVariable UUID id, @RequestBody TransactionHistory transactionHistory) {
        if (!transactionHistoryService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        transactionHistory.setId(id);
        TransactionHistory updatedTransactionHistory = transactionHistoryService.save(transactionHistory);
        return ResponseEntity.ok(updatedTransactionHistory);
    }

    /**
     * Delete transaction history by ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransactionHistory(@PathVariable UUID id) {
        if (!transactionHistoryService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        transactionHistoryService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get count of all transaction histories.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> getTransactionHistoryCount() {
        long count = transactionHistoryService.count();
        return ResponseEntity.ok(count);
    }
}