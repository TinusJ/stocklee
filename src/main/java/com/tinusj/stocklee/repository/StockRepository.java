package com.tinusj.stocklee.repository;

import com.tinusj.stocklee.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository interface for Stock entity.
 */
@Repository
public interface StockRepository extends JpaRepository<Stock, UUID> {
}