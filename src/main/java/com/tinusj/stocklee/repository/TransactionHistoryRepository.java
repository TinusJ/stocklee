package com.tinusj.stocklee.repository;

import com.tinusj.stocklee.entity.TransactionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for TransactionHistory entity.
 */
@Repository
public interface TransactionHistoryRepository extends JpaRepository<TransactionHistory, UUID> {
    
    /**
     * Find all transaction history with their associated transaction entities.
     */
    @Query("SELECT th FROM TransactionHistory th JOIN FETCH th.transaction t JOIN FETCH t.user JOIN FETCH t.stock ORDER BY th.timestamp DESC")
    List<TransactionHistory> findAllWithTransaction();
    
    /**
     * Find transaction history by ID with associated transaction entity.
     */
    @Query("SELECT th FROM TransactionHistory th JOIN FETCH th.transaction t JOIN FETCH t.user JOIN FETCH t.stock WHERE th.id = :id")
    Optional<TransactionHistory> findByIdWithTransaction(@Param("id") UUID id);
}