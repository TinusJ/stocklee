package com.tinusj.stocklee.repository;

import com.tinusj.stocklee.entity.StockTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository interface for StockTransaction entity.
 */
@Repository
public interface StockTransactionRepository extends JpaRepository<StockTransaction, UUID> {
}