package com.tinusj.stocklee.web;

import com.tinusj.stocklee.dto.BuyStockDto;
import com.tinusj.stocklee.dto.OwnedStockDto;
import com.tinusj.stocklee.dto.PortfolioSummaryDto;
import com.tinusj.stocklee.dto.SellStockDto;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;

/**
 * Test controller for testing frontend functionality without authentication
 */
@Controller
@RequestMapping("/test")
@Profile("test")
public class TestController {

    @GetMapping("/dashboard")
    public String testDashboard(Model model) {
        // Create mock data for testing
        PortfolioSummaryDto portfolio = new PortfolioSummaryDto();
        portfolio.setAvailableBalance(BigDecimal.valueOf(10000.00));
        portfolio.setTotalInvestment(BigDecimal.valueOf(5000.00));
        portfolio.setCurrentValue(BigDecimal.valueOf(5500.00));
        portfolio.setProfitLoss(BigDecimal.valueOf(500.00));
        
        // Create a mock owned stock for testing sell functionality
        OwnedStockDto ownedStock = new OwnedStockDto();
        ownedStock.setId(java.util.UUID.randomUUID());
        ownedStock.setStockSymbol("TEST");
        ownedStock.setStockName("Test Stock");
        ownedStock.setQuantity(BigDecimal.valueOf(10.5));
        ownedStock.setAveragePrice(BigDecimal.valueOf(100.00));
        ownedStock.setCurrentPrice(BigDecimal.valueOf(110.00));
        ownedStock.setTotalValue(BigDecimal.valueOf(1050.00));
        ownedStock.setCurrentValue(BigDecimal.valueOf(1155.00));
        ownedStock.setProfitLoss(BigDecimal.valueOf(105.00));
        
        portfolio.setOwnedStocks(List.of(ownedStock));
        
        // Create mock user
        model.addAttribute("currentUser", new Object() {
            public String getUsername() { return "testuser"; }
        });
        model.addAttribute("portfolio", portfolio);
        model.addAttribute("buyStockDto", new BuyStockDto());
        model.addAttribute("sellStockDto", new SellStockDto());
        
        return "dashboard";
    }
}