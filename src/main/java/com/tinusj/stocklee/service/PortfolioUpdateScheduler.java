package com.tinusj.stocklee.service;

import com.tinusj.stocklee.controller.StockPriceWebSocketController;
import com.tinusj.stocklee.dto.StockPriceUpdateDto;
import com.tinusj.stocklee.entity.Stock;
import com.tinusj.stocklee.entity.StockHistory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service for scheduled portfolio updates.
 * Updates stock prices every 10 seconds using the composite API provider setup.
 * Also handles periodic historical data updates.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioUpdateScheduler {

    private final StockService stockService;
    private final CompositeStockPriceProvider compositeStockPriceProvider;
    private final StockHistoryService stockHistoryService;
    private final StockPriceWebSocketController webSocketController;

    /**
     * Scheduled task to update portfolio every 10 seconds.
     * Fetches live stock prices and updates currentPrice and previousPrice fields.
     */
    @Scheduled(fixedRate = 10000) // 10 seconds
    @Transactional
    public void updatePortfolio() {
        log.debug("Starting portfolio update...");
        
        List<Stock> stocks = stockService.findAll();
        int successCount = 0;
        int failureCount = 0;
        
        for (Stock stock : stocks) {
            try {
                // Fetch new price from API
                Optional<BigDecimal> newPriceOpt = compositeStockPriceProvider.getPrice(stock.getSymbol());
                
                if (newPriceOpt.isPresent()) {
                    BigDecimal newPrice = newPriceOpt.get();
                    
                    // Store current price as previous price
                    stock.setPreviousPrice(stock.getCurrentPrice());
                    
                    // Update current price with new price
                    stock.setCurrentPrice(newPrice);
                    
                    // Save updated stock
                    stockService.save(stock);
                    
                    // Store price update in history (for intraday tracking)
                    StockHistory history = new StockHistory(stock, newPrice);
                    stockHistoryService.save(history);
                    
                    // Broadcast real-time update via WebSocket
                    StockPriceUpdateDto priceUpdate = new StockPriceUpdateDto(
                            stock.getSymbol(),
                            stock.getName(),
                            stock.getCurrentPrice(),
                            stock.getPreviousPrice()
                    );
                    webSocketController.broadcastStockPriceUpdate(priceUpdate);
                    
                    successCount++;
                    log.debug("Updated price for {}: {} -> {}", 
                            stock.getSymbol(), stock.getPreviousPrice(), stock.getCurrentPrice());
                } else {
                    failureCount++;
                    log.warn("Failed to fetch price for stock: {}", stock.getSymbol());
                }
            } catch (Exception e) {
                failureCount++;
                log.error("Error updating price for stock {}: {}", stock.getSymbol(), e.getMessage());
            }
        }
        
        log.info("Portfolio update completed. Updated: {}, Failed: {}", successCount, failureCount);
    }

    /**
     * Scheduled task to fetch and store historical data.
     * Runs daily to ensure we have complete historical data.
     */
    @Scheduled(cron = "${stocklee.scheduler.market-data-fetch-cron:0 0 18 * * MON-FRI}") // 6 PM on weekdays
    @Transactional
    public void updateHistoricalData() {
        log.info("Starting daily historical data update...");
        
        List<Stock> stocks = stockService.findAll();
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        
        int successCount = 0;
        int failureCount = 0;
        
        for (Stock stock : stocks) {
            try {
                // Fetch historical data for the last few days to ensure we don't miss any
                stockHistoryService.fetchAndStoreHistoricalData(stock, yesterday.minusDays(2), today);
                successCount++;
                log.debug("Updated historical data for {}", stock.getSymbol());
            } catch (Exception e) {
                failureCount++;
                log.error("Error updating historical data for stock {}: {}", stock.getSymbol(), e.getMessage());
            }
        }
        
        log.info("Historical data update completed. Updated: {}, Failed: {}", successCount, failureCount);
    }

    /**
     * Weekly task to fetch longer historical data for charting.
     * Runs on Sunday to prepare data for the upcoming week.
     */
    @Scheduled(cron = "0 0 20 * * SUN") // 8 PM on Sundays
    @Transactional
    public void updateWeeklyHistoricalData() {
        log.info("Starting weekly historical data update...");
        
        List<Stock> stocks = stockService.findAll();
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysAgo = today.minusDays(30);
        
        int successCount = 0;
        int failureCount = 0;
        
        for (Stock stock : stocks) {
            try {
                // Fetch last 30 days of historical data
                stockHistoryService.fetchAndStoreHistoricalData(stock, thirtyDaysAgo, today);
                successCount++;
                log.debug("Updated 30-day historical data for {}", stock.getSymbol());
            } catch (Exception e) {
                failureCount++;
                log.error("Error updating weekly historical data for stock {}: {}", stock.getSymbol(), e.getMessage());
            }
        }
        
        log.info("Weekly historical data update completed. Updated: {}, Failed: {}", successCount, failureCount);
    }
}