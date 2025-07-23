package com.tinusj.stocklee.service;

import com.tinusj.stocklee.entity.HistoryLog;
import com.tinusj.stocklee.repository.HistoryLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service layer for HistoryLog entity operations.
 */
@Service
@RequiredArgsConstructor
public class HistoryLogService {

    private final HistoryLogRepository historyLogRepository;

    /**
     * Find all history logs.
     */
    public List<HistoryLog> findAll() {
        return historyLogRepository.findAll();
    }

    /**
     * Find history log by ID.
     */
    public Optional<HistoryLog> findById(UUID id) {
        return historyLogRepository.findById(id);
    }

    /**
     * Save or update a history log.
     */
    public HistoryLog save(HistoryLog historyLog) {
        return historyLogRepository.save(historyLog);
    }

    /**
     * Delete history log by ID.
     */
    public void deleteById(UUID id) {
        historyLogRepository.deleteById(id);
    }

    /**
     * Check if history log exists by ID.
     */
    public boolean existsById(UUID id) {
        return historyLogRepository.existsById(id);
    }

    /**
     * Get count of all history logs.
     */
    public long count() {
        return historyLogRepository.count();
    }
}