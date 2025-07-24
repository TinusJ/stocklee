package com.tinusj.stocklee.controller;

import com.tinusj.stocklee.dto.StockPriceUpdateDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * WebSocket controller for broadcasting real-time stock price updates.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class StockPriceWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Broadcast stock price update to all connected clients.
     */
    public void broadcastStockPriceUpdate(StockPriceUpdateDto priceUpdate) {
        try {
            log.debug("Broadcasting price update for {}: {} -> {}", 
                    priceUpdate.getSymbol(), 
                    priceUpdate.getPreviousPrice(), 
                    priceUpdate.getCurrentPrice());
            
            // Broadcast to all clients subscribed to /topic/stock-prices
            messagingTemplate.convertAndSend("/topic/stock-prices", priceUpdate);
            
            // Also broadcast to symbol-specific topic for targeted updates
            messagingTemplate.convertAndSend("/topic/stock-prices/" + priceUpdate.getSymbol().toLowerCase(), priceUpdate);
            
        } catch (Exception e) {
            log.error("Error broadcasting stock price update for {}: {}", priceUpdate.getSymbol(), e.getMessage(), e);
        }
    }
}