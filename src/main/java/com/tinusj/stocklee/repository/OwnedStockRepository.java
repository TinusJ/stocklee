package com.tinusj.stocklee.repository;

import com.tinusj.stocklee.entity.OwnedStock;
import com.tinusj.stocklee.entity.Stock;
import com.tinusj.stocklee.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for OwnedStock entity.
 */
@Repository
public interface OwnedStockRepository extends JpaRepository<OwnedStock, UUID> {
    
    /**
     * Find all owned stocks for a specific user.
     */
    List<OwnedStock> findByUser(UserProfile user);
    
    /**
     * Find owned stock by user and stock.
     */
    Optional<OwnedStock> findByUserAndStock(UserProfile user, Stock stock);
    
    /**
     * Find all owned stocks for a user with stock details.
     */
    @Query("SELECT os FROM OwnedStock os JOIN FETCH os.stock WHERE os.user = :user")
    List<OwnedStock> findByUserWithStockDetails(@Param("user") UserProfile user);
}