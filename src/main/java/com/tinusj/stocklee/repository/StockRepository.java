package com.tinusj.stocklee.repository;

import com.tinusj.stocklee.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Stock entity.
 */
@Repository
public interface StockRepository extends JpaRepository<Stock, UUID> {
    
    /**
     * Find stock by symbol.
     */
    Optional<Stock> findBySymbol(String symbol);
    
    /**
     * Check if stock exists by symbol.
     */
    boolean existsBySymbol(String symbol);
}