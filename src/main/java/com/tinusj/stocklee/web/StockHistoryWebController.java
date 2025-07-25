package com.tinusj.stocklee.web;

import com.tinusj.stocklee.entity.Stock;
import com.tinusj.stocklee.entity.StockHistory;
import com.tinusj.stocklee.service.StockHistoryService;
import com.tinusj.stocklee.service.StockService;
import com.tinusj.stocklee.service.StockPriceProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Web controller for Stock History pages.
 */
@Controller
@RequestMapping("/stock-history")
@RequiredArgsConstructor
public class StockHistoryWebController {

    private final StockHistoryService stockHistoryService;
    private final StockService stockService;
    private final StockPriceProvider stockPriceProvider;

    /**
     * List stock history with filtering by stock symbol.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String listStockHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) UUID stockId,
            @RequestParam(required = false) String symbol,
            Model model) {
        
        // Create pageable with sorting
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        // Get all stock history
        List<StockHistory> stockHistories = new ArrayList<>(stockHistoryService.findAll());
        
        // Filter by stock if specified
        if (stockId != null) {
            stockHistories = new ArrayList<>(stockHistories.stream()
                .filter(history -> history.getStock().getId().equals(stockId))
                .toList());
        } else if (symbol != null && !symbol.trim().isEmpty()) {
            stockHistories = new ArrayList<>(stockHistories.stream()
                .filter(history -> history.getStock().getSymbol().equalsIgnoreCase(symbol.trim()))
                .toList());
        }
        
        // Sort manually for now
        stockHistories.sort((h1, h2) -> {
            if (sortBy.equals("timestamp")) {
                int result = h1.getTimestamp().compareTo(h2.getTimestamp());
                return direction == Sort.Direction.DESC ? -result : result;
            } else if (sortBy.equals("price")) {
                int result = h1.getPrice().compareTo(h2.getPrice());
                return direction == Sort.Direction.DESC ? -result : result;
            }
            return 0;
        });
        
        // Get all stocks for filter dropdown
        List<Stock> stocks = stockService.findAll();
        
        model.addAttribute("stockHistories", stockHistories);
        model.addAttribute("stocks", stocks);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("selectedStockId", stockId);
        model.addAttribute("selectedSymbol", symbol);
        model.addAttribute("pageTitle", "Stock History");
        
        return "stock-history/list";
    }

    /**
     * Show detailed stock history for a specific stock.
     */
    @GetMapping("/stock/{stockId}")
    @PreAuthorize("hasRole('ADMIN')")
    public String showStockHistory(@PathVariable UUID stockId, Model model) {
        Stock stock = stockService.findById(stockId)
                .orElseThrow(() -> new RuntimeException("Stock not found with id: " + stockId));
        
        // Get history for this stock
        List<StockHistory> stockHistories = new ArrayList<>(stockHistoryService.findAll().stream()
            .filter(history -> history.getStock().getId().equals(stockId))
            .sorted((h1, h2) -> h2.getTimestamp().compareTo(h1.getTimestamp())) // Latest first
            .toList());
        
        // Get current price
        var currentPrice = stockPriceProvider.getPrice(stock.getSymbol());
        
        model.addAttribute("stock", stock);
        model.addAttribute("stockHistories", stockHistories);
        model.addAttribute("currentPrice", currentPrice.orElse(null));
        model.addAttribute("pageTitle", "Stock History - " + stock.getSymbol());
        
        return "stock-history/details";
    }
}