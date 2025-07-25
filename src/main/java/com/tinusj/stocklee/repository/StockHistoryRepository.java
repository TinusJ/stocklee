package com.tinusj.stocklee.repository;

import com.tinusj.stocklee.entity.Stock;
import com.tinusj.stocklee.entity.StockHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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

    /**
     * Find historical data for a specific stock and date range.
     */
    @Query("SELECT sh FROM StockHistory sh WHERE sh.stock = :stock AND sh.date BETWEEN :fromDate AND :toDate ORDER BY sh.date DESC")
    List<StockHistory> findByStockAndDateBetween(@Param("stock") Stock stock, 
                                                @Param("fromDate") LocalDate fromDate, 
                                                @Param("toDate") LocalDate toDate);

    /**
     * Find historical data for a specific stock symbol and date range.
     */
    @Query("SELECT sh FROM StockHistory sh JOIN FETCH sh.stock s WHERE s.symbol = :symbol AND sh.date BETWEEN :fromDate AND :toDate ORDER BY sh.date DESC")
    List<StockHistory> findByStockSymbolAndDateBetween(@Param("symbol") String symbol, 
                                                       @Param("fromDate") LocalDate fromDate, 
                                                       @Param("toDate") LocalDate toDate);

    /**
     * Find latest historical data for a specific stock.
     */
    @Query("SELECT sh FROM StockHistory sh WHERE sh.stock = :stock ORDER BY sh.date DESC, sh.timestamp DESC")
    List<StockHistory> findLatestByStock(@Param("stock") Stock stock);

    /**
     * Find historical data for a specific stock by date.
     */
    @Query("SELECT sh FROM StockHistory sh WHERE sh.stock = :stock AND sh.date = :date")
    List<StockHistory> findByStockAndDate(@Param("stock") Stock stock, @Param("date") LocalDate date);
}