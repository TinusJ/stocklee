package com.tinusj.stocklee.repository;

import com.tinusj.stocklee.entity.TransactionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository interface for TransactionHistory entity.
 */
@Repository
public interface TransactionHistoryRepository extends JpaRepository<TransactionHistory, UUID> {
}