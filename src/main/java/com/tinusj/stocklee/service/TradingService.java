package com.tinusj.stocklee.service;

import com.tinusj.stocklee.dto.BuyStockDto;
import com.tinusj.stocklee.dto.PortfolioSummaryDto;
import com.tinusj.stocklee.dto.SellStockDto;
import com.tinusj.stocklee.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Service for handling stock trading operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TradingService {

    private final StockService stockService;
    private final OwnedStockService ownedStockService;
    private final UserProfileService userProfileService;
    private final StockTransactionService stockTransactionService;
    private final HistoryLogService historyLogService;
    private final YahooFinanceService yahooFinanceService;

    /**
     * Buy stocks for a user.
     */
    public String buyStock(UserProfile user, BuyStockDto buyStockDto) {
        try {
            // Calculate current balance if null
            BigDecimal availableBalance = calculateUserBalance(user);
            
            // Validate user has sufficient balance
            if (availableBalance.compareTo(buyStockDto.getInvestmentAmount()) < 0) {
                return "Insufficient balance. Available: $" + availableBalance;
            }

            // Check if stock exists or create new one
            Stock stock = stockService.findBySymbol(buyStockDto.getSymbol())
                    .orElseGet(() -> createNewStock(buyStockDto.getSymbol()));

            if (stock == null) {
                return "Invalid stock symbol: " + buyStockDto.getSymbol();
            }

            // Calculate number of shares (allow fractional)
            BigDecimal currentPrice = stock.getCurrentPrice();
            BigDecimal shares = buyStockDto.getInvestmentAmount().divide(currentPrice, 4, java.math.RoundingMode.DOWN);
            
            if (shares.compareTo(BigDecimal.valueOf(0.0001)) < 0) {
                return "Investment amount too small. Minimum required: $" + currentPrice.multiply(BigDecimal.valueOf(0.0001));
            }

            BigDecimal actualCost = currentPrice.multiply(shares);

            // Update user balance
            BigDecimal newBalance = availableBalance.subtract(actualCost);
            user.setBalance(newBalance);
            userProfileService.save(user);

            // Add shares to portfolio
            ownedStockService.addShares(user, stock, shares, currentPrice);

            // Record transaction
            StockTransaction transaction = new StockTransaction();
            transaction.setUser(user);
            transaction.setStock(stock);
            transaction.setTransactionType(StockTransaction.TransactionType.BUY);
            transaction.setQuantity(shares);
            transaction.setPrice(currentPrice);
            transaction.setTotalValue(actualCost);
            stockTransactionService.save(transaction);

            // Log activity
            String logMessage = String.format("Bought %.4f shares of %s at $%.2f per share (Total: $%.2f)", 
                    shares, stock.getSymbol(), currentPrice, actualCost);
            logActivity(user, logMessage);

            return String.format("Successfully bought %.4f shares of %s for $%.2f", shares, stock.getSymbol(), actualCost);

        } catch (Exception e) {
            log.error("Error buying stock for user {}: {}", user.getUsername(), e.getMessage(), e);
            return "Error processing purchase: " + e.getMessage();
        }
    }

    /**
     * Sell stocks for a user.
     */
    public String sellStock(UserProfile user, SellStockDto sellStockDto) {
        try {
            Optional<OwnedStock> ownedStockOpt = ownedStockService.findById(sellStockDto.getOwnedStockId());
            if (ownedStockOpt.isEmpty()) {
                return "Owned stock not found";
            }

            OwnedStock ownedStock = ownedStockOpt.get();
            if (!ownedStock.getUser().getId().equals(user.getId())) {
                return "You don't own this stock";
            }

            if (ownedStock.getQuantity().compareTo(sellStockDto.getQuantity()) < 0) {
                return "Insufficient shares. You own: " + ownedStock.getQuantity();
            }

            Stock stock = ownedStock.getStock();
            BigDecimal currentPrice = stock.getCurrentPrice();
            BigDecimal sellValue = currentPrice.multiply(sellStockDto.getQuantity());

            // Update user balance
            BigDecimal currentBalance = calculateUserBalance(user);
            user.setBalance(currentBalance.add(sellValue));
            userProfileService.save(user);

            // Remove shares from portfolio
            ownedStockService.removeShares(user, stock, sellStockDto.getQuantity());

            // Record transaction
            StockTransaction transaction = new StockTransaction();
            transaction.setUser(user);
            transaction.setStock(stock);
            transaction.setTransactionType(StockTransaction.TransactionType.SELL);
            transaction.setQuantity(sellStockDto.getQuantity());
            transaction.setPrice(currentPrice);
            transaction.setTotalValue(sellValue);
            stockTransactionService.save(transaction);

            // Log activity
            String logMessage = String.format("Sold %.4f shares of %s at $%.2f per share (Total: $%.2f)", 
                    sellStockDto.getQuantity(), stock.getSymbol(), currentPrice, sellValue);
            logActivity(user, logMessage);

            return String.format("Successfully sold %.4f shares of %s for $%.2f", 
                    sellStockDto.getQuantity(), stock.getSymbol(), sellValue);

        } catch (Exception e) {
            log.error("Error selling stock for user {}: {}", user.getUsername(), e.getMessage(), e);
            return "Error processing sale: " + e.getMessage();
        }
    }

    /**
     * Get portfolio summary for a user.
     */
    public PortfolioSummaryDto getPortfolioSummary(UserProfile user) {
        PortfolioSummaryDto summary = new PortfolioSummaryDto();
        summary.setAvailableBalance(calculateUserBalance(user));
        summary.setTotalInvestment(ownedStockService.calculateTotalInvestment(user));
        summary.setCurrentValue(ownedStockService.calculateTotalPortfolioValue(user));
        summary.setProfitLoss(ownedStockService.calculateProfitLoss(user));
        
        // Convert owned stocks to DTOs
        summary.setOwnedStocks(
            ownedStockService.findByUser(user).stream()
                .map(this::convertOwnedStockToDto)
                .toList()
        );
        
        return summary;
    }

    /**
     * Calculate user's current balance dynamically.
     * If balance is null, calculate from transactions.
     */
    private BigDecimal calculateUserBalance(UserProfile user) {
        if (user.getBalance() != null) {
            return user.getBalance();
        }
        
        // Calculate balance from transactions if not set
        // This would be the initial balance (0) plus any deposits minus any stock purchases plus any stock sales
        // For simplicity, we'll start with 0 and calculate from stock transactions
        return BigDecimal.ZERO; // In a real app, you might want to set a default starting balance
    }

    private Stock createNewStock(String symbol) {
        if (!yahooFinanceService.isValidSymbol(symbol)) {
            return null;
        }

        Optional<BigDecimal> priceOpt = yahooFinanceService.getCurrentPrice(symbol);
        if (priceOpt.isEmpty()) {
            return null;
        }

        Stock stock = new Stock();
        stock.setSymbol(symbol.toUpperCase());
        stock.setName(symbol.toUpperCase() + " Stock"); // Simplified name
        stock.setCurrentPrice(priceOpt.get());
        stock.setMarket(Stock.MarketType.OTHER);
        stock.setDescription("Auto-created stock for symbol: " + symbol);

        return stockService.save(stock);
    }

    private void logActivity(UserProfile user, String message) {
        HistoryLog log = new HistoryLog();
        log.setUser(user);
        log.setAction(message);
        historyLogService.save(log);
    }

    private com.tinusj.stocklee.dto.OwnedStockDto convertOwnedStockToDto(OwnedStock ownedStock) {
        com.tinusj.stocklee.dto.OwnedStockDto dto = new com.tinusj.stocklee.dto.OwnedStockDto();
        dto.setId(ownedStock.getId());
        dto.setQuantity(ownedStock.getQuantity());
        dto.setAveragePrice(ownedStock.getAveragePrice());
        dto.setTotalValue(ownedStock.getTotalValue());
        
        // Add stock details
        Stock stock = ownedStock.getStock();
        dto.setStockSymbol(stock.getSymbol());
        dto.setStockName(stock.getName());
        dto.setCurrentPrice(stock.getCurrentPrice());
        
        // Calculate current value and profit/loss
        BigDecimal currentValue = stock.getCurrentPrice().multiply(ownedStock.getQuantity());
        dto.setCurrentValue(currentValue);
        dto.setProfitLoss(currentValue.subtract(ownedStock.getTotalValue()));
        
        return dto;
    }
}