package com.tinusj.stocklee.repository;

import com.tinusj.stocklee.entity.StockHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for StockHistory entity.
 */
@Repository
public interface StockHistoryRepository extends JpaRepository<StockHistory, UUID> {
    
    /**
     * Find all stock history with their associated stock entities.
     */
    @Query("SELECT sh FROM StockHistory sh JOIN FETCH sh.stock ORDER BY sh.timestamp DESC")
    List<StockHistory> findAllWithStock();
    
    /**
     * Find stock history by ID with associated stock entity.
     */
    @Query("SELECT sh FROM StockHistory sh JOIN FETCH sh.stock WHERE sh.id = :id")
    Optional<StockHistory> findByIdWithStock(@Param("id") UUID id);
}