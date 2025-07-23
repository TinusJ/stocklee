package com.tinusj.stocklee.repository;

import com.tinusj.stocklee.entity.OwnedStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository interface for OwnedStock entity.
 */
@Repository
public interface OwnedStockRepository extends JpaRepository<OwnedStock, UUID> {
}