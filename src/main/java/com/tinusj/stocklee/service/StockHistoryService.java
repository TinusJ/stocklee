package com.tinusj.stocklee.service;

import com.tinusj.stocklee.entity.Stock;
import com.tinusj.stocklee.entity.StockHistory;
import com.tinusj.stocklee.repository.StockHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service layer for StockHistory entity operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StockHistoryService {

    private final StockHistoryRepository stockHistoryRepository;
    
    @Autowired
    @Lazy
    private NasdaqApiService nasdaqApiService;

    /**
     * Find all stock histories.
     */
    public List<StockHistory> findAll() {
        return stockHistoryRepository.findAllWithStock();
    }

    /**
     * Find stock history by ID.
     */
    public Optional<StockHistory> findById(UUID id) {
        return stockHistoryRepository.findByIdWithStock(id);
    }

    /**
     * Save or update a stock history.
     */
    public StockHistory save(StockHistory stockHistory) {
        return stockHistoryRepository.save(stockHistory);
    }

    /**
     * Delete stock history by ID.
     */
    public void deleteById(UUID id) {
        stockHistoryRepository.deleteById(id);
    }

    /**
     * Check if stock history exists by ID.
     */
    public boolean existsById(UUID id) {
        return stockHistoryRepository.existsById(id);
    }

    /**
     * Get count of all stock histories.
     */
    public long count() {
        return stockHistoryRepository.count();
    }

    /**
     * Get historical data for a stock within a date range.
     */
    public List<StockHistory> getHistoricalData(String symbol, LocalDate fromDate, LocalDate toDate) {
        return stockHistoryRepository.findByStockSymbolAndDateBetween(symbol, fromDate, toDate);
    }

    /**
     * Get historical data for a stock within a date range.
     */
    public List<StockHistory> getHistoricalData(Stock stock, LocalDate fromDate, LocalDate toDate) {
        return stockHistoryRepository.findByStockAndDateBetween(stock, fromDate, toDate);
    }

    /**
     * Get latest historical data for a stock.
     */
    public List<StockHistory> getLatestHistoricalData(Stock stock) {
        return stockHistoryRepository.findLatestByStock(stock);
    }

    /**
     * Fetch and store historical data from Nasdaq API.
     */
    public void fetchAndStoreHistoricalData(Stock stock, LocalDate fromDate, LocalDate toDate) {
        try {
            if (nasdaqApiService == null) {
                log.warn("NasdaqApiService not available, skipping historical data fetch for {}", stock.getSymbol());
                return;
            }
            
            log.info("Fetching historical data for {} from {} to {}", stock.getSymbol(), fromDate, toDate);
            
            List<NasdaqApiService.HistoricalStockData> historicalData = 
                nasdaqApiService.getHistoricalData(stock.getSymbol(), fromDate, toDate);
            
            for (NasdaqApiService.HistoricalStockData data : historicalData) {
                // Check if we already have data for this date
                List<StockHistory> existingData = stockHistoryRepository.findByStockAndDate(stock, data.getDate());
                
                if (existingData.isEmpty()) {
                    StockHistory stockHistory = new StockHistory(
                        stock,
                        data.getDate(),
                        data.getOpenPrice(),
                        data.getHighPrice(),
                        data.getLowPrice(),
                        data.getClosePrice(),
                        data.getVolume()
                    );
                    
                    stockHistoryRepository.save(stockHistory);
                    log.debug("Stored historical data for {} on {}: Close ${}", 
                        stock.getSymbol(), data.getDate(), data.getClosePrice());
                } else {
                    log.debug("Historical data already exists for {} on {}", stock.getSymbol(), data.getDate());
                }
            }
            
            log.info("Completed storing historical data for {}: {} records processed", 
                stock.getSymbol(), historicalData.size());
            
        } catch (Exception e) {
            log.error("Error fetching and storing historical data for stock {}: {}", stock.getSymbol(), e.getMessage());
        }
    }
}