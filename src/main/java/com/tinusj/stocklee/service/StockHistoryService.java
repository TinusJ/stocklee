package com.tinusj.stocklee.service;

import com.tinusj.stocklee.entity.StockHistory;
import com.tinusj.stocklee.repository.StockHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service layer for StockHistory entity operations.
 */
@Service
@RequiredArgsConstructor
public class StockHistoryService {

    private final StockHistoryRepository stockHistoryRepository;

    /**
     * Find all stock histories.
     */
    public List<StockHistory> findAll() {
        return stockHistoryRepository.findAll();
    }

    /**
     * Find stock history by ID.
     */
    public Optional<StockHistory> findById(UUID id) {
        return stockHistoryRepository.findById(id);
    }

    /**
     * Save or update a stock history.
     */
    public StockHistory save(StockHistory stockHistory) {
        return stockHistoryRepository.save(stockHistory);
    }

    /**
     * Delete stock history by ID.
     */
    public void deleteById(UUID id) {
        stockHistoryRepository.deleteById(id);
    }

    /**
     * Check if stock history exists by ID.
     */
    public boolean existsById(UUID id) {
        return stockHistoryRepository.existsById(id);
    }

    /**
     * Get count of all stock histories.
     */
    public long count() {
        return stockHistoryRepository.count();
    }
}