package com.tinusj.stocklee.repository;

import com.tinusj.stocklee.entity.StockHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository interface for StockHistory entity.
 */
@Repository
public interface StockHistoryRepository extends JpaRepository<StockHistory, UUID> {
}