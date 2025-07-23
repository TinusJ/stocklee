package com.tinusj.stocklee.web;

import com.tinusj.stocklee.entity.OwnedStock;
import com.tinusj.stocklee.entity.UserProfile;
import com.tinusj.stocklee.service.OwnedStockService;
import com.tinusj.stocklee.service.CompositeStockPriceProvider;
import com.tinusj.stocklee.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

/**
 * Web controller for Owned Stocks pages.
 */
@Controller
@RequestMapping("/owned-stocks")
@RequiredArgsConstructor
public class OwnedStockWebController {

    private final OwnedStockService ownedStockService;
    private final UserProfileService userProfileService;
    private final CompositeStockPriceProvider stockPriceProvider;

    /**
     * List all owned stocks with current prices and profit/loss calculations.
     */
    @GetMapping
    public String listOwnedStocks(
            @RequestParam(required = false) UUID userId,
            Model model) {
        
        List<OwnedStock> ownedStocks = ownedStockService.findAll();
        
        // Filter by user if specified
        if (userId != null) {
            ownedStocks = ownedStocks.stream()
                .filter(os -> os.getUser().getId().equals(userId))
                .toList();
        }
        
        // Calculate current values and profit/loss for each owned stock
        ownedStocks.forEach(this::calculateCurrentValues);
        
        // Get all users for filter dropdown
        List<UserProfile> users = userProfileService.findAll();
        
        // Calculate totals
        BigDecimal totalInvestment = ownedStocks.stream()
            .map(os -> os.getAveragePrice().multiply(os.getQuantity()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        BigDecimal totalCurrentValue = ownedStocks.stream()
            .map(os -> {
                BigDecimal currentPrice = stockPriceProvider.getPrice(os.getStock().getSymbol())
                    .orElse(os.getAveragePrice());
                return currentPrice.multiply(os.getQuantity());
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        BigDecimal totalProfitLoss = totalCurrentValue.subtract(totalInvestment);
        
        model.addAttribute("ownedStocks", ownedStocks);
        model.addAttribute("users", users);
        model.addAttribute("selectedUserId", userId);
        model.addAttribute("totalInvestment", totalInvestment);
        model.addAttribute("totalCurrentValue", totalCurrentValue);
        model.addAttribute("totalProfitLoss", totalProfitLoss);
        model.addAttribute("pageTitle", "Owned Stocks");
        
        return "owned-stock/list";
    }

    /**
     * Show owned stock details.
     */
    @GetMapping("/{id}")
    public String showOwnedStock(@PathVariable UUID id, Model model) {
        OwnedStock ownedStock = ownedStockService.findById(id)
                .orElseThrow(() -> new RuntimeException("Owned stock not found with id: " + id));
        
        calculateCurrentValues(ownedStock);
        
        model.addAttribute("ownedStock", ownedStock);
        model.addAttribute("pageTitle", "Owned Stock Details");
        
        return "owned-stock/details";
    }

    /**
     * Calculate current values for an owned stock.
     */
    private void calculateCurrentValues(OwnedStock ownedStock) {
        BigDecimal currentPrice = stockPriceProvider.getPrice(ownedStock.getStock().getSymbol())
            .orElse(ownedStock.getAveragePrice());
        
        BigDecimal currentValue = currentPrice.multiply(ownedStock.getQuantity());
        BigDecimal investment = ownedStock.getAveragePrice().multiply(ownedStock.getQuantity());
        BigDecimal profitLoss = currentValue.subtract(investment);
        
        // Add calculated values as transient properties (these won't be persisted)
        ownedStock.setCurrentPrice(currentPrice);
        ownedStock.setCurrentValue(currentValue);
        ownedStock.setProfitLoss(profitLoss);
        
        // Calculate profit/loss percentage
        if (investment.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal profitLossPercentage = profitLoss.divide(investment, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
            ownedStock.setProfitLossPercentage(profitLossPercentage);
        } else {
            ownedStock.setProfitLossPercentage(BigDecimal.ZERO);
        }
    }
}