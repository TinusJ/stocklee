package com.tinusj.stocklee.repository;

import com.tinusj.stocklee.entity.StockTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for StockTransaction entity.
 */
@Repository
public interface StockTransactionRepository extends JpaRepository<StockTransaction, UUID> {
    
    /**
     * Find all stock transactions with their associated user and stock entities.
     */
    @Query("SELECT st FROM StockTransaction st JOIN FETCH st.user JOIN FETCH st.stock ORDER BY st.timestamp DESC")
    List<StockTransaction> findAllWithUserAndStock();
    
    /**
     * Find stock transaction by ID with associated user and stock entities.
     */
    @Query("SELECT st FROM StockTransaction st JOIN FETCH st.user JOIN FETCH st.stock WHERE st.id = :id")
    Optional<StockTransaction> findByIdWithUserAndStock(@Param("id") UUID id);
    
    /**
     * Find all stock transactions for a specific user.
     */
    @Query("SELECT st FROM StockTransaction st JOIN FETCH st.stock WHERE st.user = :user ORDER BY st.timestamp DESC")
    List<StockTransaction> findByUserWithStockDetails(@Param("user") com.tinusj.stocklee.entity.UserProfile user);
}