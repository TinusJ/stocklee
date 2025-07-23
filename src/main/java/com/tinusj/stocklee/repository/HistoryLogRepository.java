package com.tinusj.stocklee.repository;

import com.tinusj.stocklee.entity.HistoryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository interface for HistoryLog entity.
 */
@Repository
public interface HistoryLogRepository extends JpaRepository<HistoryLog, UUID> {
}