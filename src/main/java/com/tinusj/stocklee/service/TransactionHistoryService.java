package com.tinusj.stocklee.service;

import com.tinusj.stocklee.entity.TransactionHistory;
import com.tinusj.stocklee.repository.TransactionHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service layer for TransactionHistory entity operations.
 */
@Service
@RequiredArgsConstructor
public class TransactionHistoryService {

    private final TransactionHistoryRepository transactionHistoryRepository;

    /**
     * Find all transaction histories.
     */
    public List<TransactionHistory> findAll() {
        return transactionHistoryRepository.findAll();
    }

    /**
     * Find transaction history by ID.
     */
    public Optional<TransactionHistory> findById(UUID id) {
        return transactionHistoryRepository.findById(id);
    }

    /**
     * Save or update a transaction history.
     */
    public TransactionHistory save(TransactionHistory transactionHistory) {
        return transactionHistoryRepository.save(transactionHistory);
    }

    /**
     * Delete transaction history by ID.
     */
    public void deleteById(UUID id) {
        transactionHistoryRepository.deleteById(id);
    }

    /**
     * Check if transaction history exists by ID.
     */
    public boolean existsById(UUID id) {
        return transactionHistoryRepository.existsById(id);
    }

    /**
     * Get count of all transaction histories.
     */
    public long count() {
        return transactionHistoryRepository.count();
    }
}