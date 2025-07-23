package com.tinusj.stocklee.service;

import com.tinusj.stocklee.entity.OwnedStock;
import com.tinusj.stocklee.repository.OwnedStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service layer for OwnedStock entity operations.
 */
@Service
@RequiredArgsConstructor
public class OwnedStockService {

    private final OwnedStockRepository ownedStockRepository;

    /**
     * Find all owned stocks.
     */
    public List<OwnedStock> findAll() {
        return ownedStockRepository.findAll();
    }

    /**
     * Find owned stock by ID.
     */
    public Optional<OwnedStock> findById(UUID id) {
        return ownedStockRepository.findById(id);
    }

    /**
     * Save or update an owned stock.
     */
    public OwnedStock save(OwnedStock ownedStock) {
        return ownedStockRepository.save(ownedStock);
    }

    /**
     * Delete owned stock by ID.
     */
    public void deleteById(UUID id) {
        ownedStockRepository.deleteById(id);
    }

    /**
     * Check if owned stock exists by ID.
     */
    public boolean existsById(UUID id) {
        return ownedStockRepository.existsById(id);
    }

    /**
     * Get count of all owned stocks.
     */
    public long count() {
        return ownedStockRepository.count();
    }
}