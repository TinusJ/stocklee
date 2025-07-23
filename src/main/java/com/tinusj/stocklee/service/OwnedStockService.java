package com.tinusj.stocklee.service;

import com.tinusj.stocklee.entity.OwnedStock;
import com.tinusj.stocklee.entity.Stock;
import com.tinusj.stocklee.entity.UserProfile;
import com.tinusj.stocklee.repository.OwnedStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service layer for OwnedStock entity operations.
 */
@Service
@RequiredArgsConstructor
public class OwnedStockService {

    private final OwnedStockRepository ownedStockRepository;

    /**
     * Find all owned stocks.
     */
    public List<OwnedStock> findAll() {
        return ownedStockRepository.findAll();
    }

    /**
     * Find owned stock by ID.
     */
    public Optional<OwnedStock> findById(UUID id) {
        return ownedStockRepository.findById(id);
    }

    /**
     * Save or update an owned stock.
     */
    public OwnedStock save(OwnedStock ownedStock) {
        return ownedStockRepository.save(ownedStock);
    }

    /**
     * Delete owned stock by ID.
     */
    public void deleteById(UUID id) {
        ownedStockRepository.deleteById(id);
    }

    /**
     * Check if owned stock exists by ID.
     */
    public boolean existsById(UUID id) {
        return ownedStockRepository.existsById(id);
    }

    /**
     * Get count of all owned stocks.
     */
    public long count() {
        return ownedStockRepository.count();
    }

    /**
     * Find all owned stocks for a specific user.
     */
    public List<OwnedStock> findByUser(UserProfile user) {
        return ownedStockRepository.findByUserWithStockDetails(user);
    }

    /**
     * Find owned stock by user and stock.
     */
    public Optional<OwnedStock> findByUserAndStock(UserProfile user, Stock stock) {
        return ownedStockRepository.findByUserAndStock(user, stock);
    }

    /**
     * Add shares to user's portfolio or create new holding.
     */
    public OwnedStock addShares(UserProfile user, Stock stock, BigDecimal quantity, BigDecimal pricePerShare) {
        Optional<OwnedStock> existingOwned = findByUserAndStock(user, stock);
        
        if (existingOwned.isPresent()) {
            // Update existing holding
            OwnedStock owned = existingOwned.get();
            BigDecimal currentTotal = owned.getAveragePrice().multiply(owned.getQuantity());
            BigDecimal newTotal = pricePerShare.multiply(quantity);
            BigDecimal combinedTotal = currentTotal.add(newTotal);
            
            BigDecimal newQuantity = owned.getQuantity().add(quantity);
            BigDecimal newAveragePrice = combinedTotal.divide(newQuantity, 4, java.math.RoundingMode.HALF_UP);
            
            owned.setQuantity(newQuantity);
            owned.setAveragePrice(newAveragePrice);
            owned.setTotalValue(combinedTotal);
            
            return save(owned);
        } else {
            // Create new holding
            OwnedStock newOwned = new OwnedStock();
            newOwned.setUser(user);
            newOwned.setStock(stock);
            newOwned.setQuantity(quantity);
            newOwned.setAveragePrice(pricePerShare);
            newOwned.setTotalValue(pricePerShare.multiply(quantity));
            
            return save(newOwned);
        }
    }

    /**
     * Remove shares from user's portfolio.
     */
    public boolean removeShares(UserProfile user, Stock stock, BigDecimal quantity) {
        Optional<OwnedStock> existingOwned = findByUserAndStock(user, stock);
        
        if (existingOwned.isEmpty()) {
            return false; // User doesn't own this stock
        }
        
        OwnedStock owned = existingOwned.get();
        if (owned.getQuantity().compareTo(quantity) < 0) {
            return false; // Not enough shares to sell
        }
        
        if (owned.getQuantity().compareTo(quantity) == 0) {
            // Selling all shares - delete the record
            deleteById(owned.getId());
        } else {
            // Selling partial shares - update the record
            BigDecimal newQuantity = owned.getQuantity().subtract(quantity);
            BigDecimal newTotalValue = owned.getAveragePrice().multiply(newQuantity);
            
            owned.setQuantity(newQuantity);
            owned.setTotalValue(newTotalValue);
            save(owned);
        }
        
        return true;
    }

    /**
     * Calculate total portfolio value for a user at current prices.
     */
    public BigDecimal calculateTotalPortfolioValue(UserProfile user) {
        List<OwnedStock> ownedStocks = findByUser(user);
        return ownedStocks.stream()
                .map(owned -> owned.getStock().getCurrentPrice().multiply(owned.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculate total investment cost for a user.
     */
    public BigDecimal calculateTotalInvestment(UserProfile user) {
        List<OwnedStock> ownedStocks = findByUser(user);
        return ownedStocks.stream()
                .map(OwnedStock::getTotalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculate profit/loss for a user.
     */
    public BigDecimal calculateProfitLoss(UserProfile user) {
        BigDecimal currentValue = calculateTotalPortfolioValue(user);
        BigDecimal investment = calculateTotalInvestment(user);
        return currentValue.subtract(investment);
    }
}