package com.tinusj.stocklee.repository;

import com.tinusj.stocklee.entity.HistoryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for HistoryLog entity.
 */
@Repository
public interface HistoryLogRepository extends JpaRepository<HistoryLog, UUID> {
    
    /**
     * Find all history logs with their associated user entities.
     */
    @Query("SELECT hl FROM HistoryLog hl JOIN FETCH hl.user ORDER BY hl.timestamp DESC")
    List<HistoryLog> findAllWithUser();
    
    /**
     * Find history log by ID with associated user entity.
     */
    @Query("SELECT hl FROM HistoryLog hl JOIN FETCH hl.user WHERE hl.id = :id")
    Optional<HistoryLog> findByIdWithUser(@Param("id") UUID id);
}