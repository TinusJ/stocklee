package com.tinusj.stocklee.service;

import com.tinusj.stocklee.controller.StockPriceWebSocketController;
import com.tinusj.stocklee.dto.StockPriceUpdateDto;
import com.tinusj.stocklee.entity.Stock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service for scheduled portfolio updates.
 * Updates stock prices every 10 seconds using the dual API provider setup.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioUpdateScheduler {

    private final StockService stockService;
    private final CompositeStockPriceProvider compositeStockPriceProvider;
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
}