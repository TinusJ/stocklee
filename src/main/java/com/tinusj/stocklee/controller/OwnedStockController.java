package com.tinusj.stocklee.controller;

import com.tinusj.stocklee.entity.OwnedStock;
import com.tinusj.stocklee.service.OwnedStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for OwnedStock entity operations.
 */
@RestController
@RequestMapping("/api/owned-stocks")
@RequiredArgsConstructor
public class OwnedStockController {

    private final OwnedStockService ownedStockService;

    /**
     * Get all owned stocks.
     */
    @GetMapping
    public ResponseEntity<List<OwnedStock>> getAllOwnedStocks() {
        List<OwnedStock> ownedStocks = ownedStockService.findAll();
        return ResponseEntity.ok(ownedStocks);
    }

    /**
     * Get owned stock by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<OwnedStock> getOwnedStockById(@PathVariable UUID id) {
        return ownedStockService.findById(id)
                .map(ownedStock -> ResponseEntity.ok(ownedStock))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new owned stock.
     */
    @PostMapping
    public ResponseEntity<OwnedStock> createOwnedStock(@RequestBody OwnedStock ownedStock) {
        OwnedStock savedOwnedStock = ownedStockService.save(ownedStock);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedOwnedStock);
    }

    /**
     * Update an existing owned stock.
     */
    @PutMapping("/{id}")
    public ResponseEntity<OwnedStock> updateOwnedStock(@PathVariable UUID id, @RequestBody OwnedStock ownedStock) {
        if (!ownedStockService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        ownedStock.setId(id);
        OwnedStock updatedOwnedStock = ownedStockService.save(ownedStock);
        return ResponseEntity.ok(updatedOwnedStock);
    }

    /**
     * Delete owned stock by ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOwnedStock(@PathVariable UUID id) {
        if (!ownedStockService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        ownedStockService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get count of all owned stocks.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> getOwnedStockCount() {
        long count = ownedStockService.count();
        return ResponseEntity.ok(count);
    }
}