package com.tinusj.stocklee.service;

import com.tinusj.stocklee.entity.StockTransaction;
import com.tinusj.stocklee.entity.UserProfile;
import com.tinusj.stocklee.repository.StockTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service layer for StockTransaction entity operations.
 */
@Service
@RequiredArgsConstructor
public class StockTransactionService {

    private final StockTransactionRepository stockTransactionRepository;

    /**
     * Find all stock transactions.
     */
    public List<StockTransaction> findAll() {
        return stockTransactionRepository.findAllWithUserAndStock();
    }

    /**
     * Find stock transaction by ID.
     */
    public Optional<StockTransaction> findById(UUID id) {
        return stockTransactionRepository.findByIdWithUserAndStock(id);
    }

    /**
     * Save or update a stock transaction.
     */
    public StockTransaction save(StockTransaction stockTransaction) {
        return stockTransactionRepository.save(stockTransaction);
    }

    /**
     * Delete stock transaction by ID.
     */
    public void deleteById(UUID id) {
        stockTransactionRepository.deleteById(id);
    }

    /**
     * Check if stock transaction exists by ID.
     */
    public boolean existsById(UUID id) {
        return stockTransactionRepository.existsById(id);
    }

    /**
     * Get count of all stock transactions.
     */
    public long count() {
        return stockTransactionRepository.count();
    }

    /**
     * Find all stock transactions for a specific user.
     */
    public List<StockTransaction> findByUser(UserProfile user) {
        return stockTransactionRepository.findByUserWithStockDetails(user);
    }
}