package com.tinusj.stocklee.controller;

import com.tinusj.stocklee.dto.CurrentPriceDto;
import com.tinusj.stocklee.dto.HistoricalStockDataDto;
import com.tinusj.stocklee.entity.Stock;
import com.tinusj.stocklee.entity.StockHistory;
import com.tinusj.stocklee.service.StockService;
import com.tinusj.stocklee.service.StockHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for Stock entity operations.
 */
@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;
    private final StockHistoryService stockHistoryService;

    /**
     * Get all stocks.
     */
    @GetMapping
    public ResponseEntity<List<Stock>> getAllStocks() {
        List<Stock> stocks = stockService.findAll();
        return ResponseEntity.ok(stocks);
    }

    /**
     * Get stock by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Stock> getStockById(@PathVariable UUID id) {
        return stockService.findById(id)
                .map(stock -> ResponseEntity.ok(stock))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new stock.
     */
    @PostMapping
    public ResponseEntity<Stock> createStock(@RequestBody Stock stock) {
        Stock savedStock = stockService.save(stock);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedStock);
    }

    /**
     * Update an existing stock.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Stock> updateStock(@PathVariable UUID id, @RequestBody Stock stock) {
        if (!stockService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        stock.setId(id);
        Stock updatedStock = stockService.save(stock);
        return ResponseEntity.ok(updatedStock);
    }

    /**
     * Delete stock by ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStock(@PathVariable UUID id) {
        if (!stockService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        stockService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get count of all stocks.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> getStockCount() {
        long count = stockService.count();
        return ResponseEntity.ok(count);
    }

    /**
     * Get current price for a stock symbol.
     * Used for prepopulating purchase price in the UI.
     */
    @GetMapping("/price/{symbol}")
    public ResponseEntity<CurrentPriceDto> getCurrentPrice(@PathVariable String symbol) {
        String upperSymbol = symbol.toUpperCase();
        
        // First try to get from database (existing stock)
        var stockOpt = stockService.findBySymbol(upperSymbol);
        if (stockOpt.isPresent()) {
            Stock stock = stockOpt.get();
            CurrentPriceDto dto = new CurrentPriceDto(
                stock.getSymbol(),
                stock.getName(),
                stock.getCurrentPrice(),
                stock.getPreviousPrice(),
                true
            );
            return ResponseEntity.ok(dto);
        }
        
        // If not in database, try to fetch from API
        var priceOpt = stockService.getCurrentPrice(upperSymbol);
        if (priceOpt.isPresent()) {
            CurrentPriceDto dto = new CurrentPriceDto(
                upperSymbol,
                upperSymbol, // We don't have name from API call
                priceOpt.get(),
                null,
                false
            );
            return ResponseEntity.ok(dto);
        }
        
        return ResponseEntity.notFound().build();
    }

    /**
     * Get latest stock data including current price.
     * This endpoint provides the most recent stock information.
     */
    @GetMapping("/{symbol}/latest")
    public ResponseEntity<CurrentPriceDto> getLatestStockData(@PathVariable String symbol) {
        return getCurrentPrice(symbol);
    }

    /**
     * Get historical data for a stock within a date range.
     */
    @GetMapping("/{symbol}/historical")
    public ResponseEntity<List<HistoricalStockDataDto>> getHistoricalData(
            @PathVariable String symbol,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        
        String upperSymbol = symbol.toUpperCase();
        
        List<StockHistory> historicalData = stockHistoryService.getHistoricalData(upperSymbol, fromDate, toDate);
        
        List<HistoricalStockDataDto> dtoList = historicalData.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtoList);
    }

    /**
     * Get historical data for the last N days.
     */
    @GetMapping("/{symbol}/historical/last/{days}")
    public ResponseEntity<List<HistoricalStockDataDto>> getHistoricalDataLastDays(
            @PathVariable String symbol,
            @PathVariable int days) {
        
        LocalDate toDate = LocalDate.now();
        LocalDate fromDate = toDate.minusDays(days);
        
        return getHistoricalData(symbol, fromDate, toDate);
    }

    /**
     * Fetch and store historical data from external API.
     */
    @PostMapping("/{symbol}/fetch-historical")
    public ResponseEntity<String> fetchHistoricalData(
            @PathVariable String symbol,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        
        String upperSymbol = symbol.toUpperCase();
        
        // Default to last 30 days if dates not provided
        if (fromDate == null) {
            fromDate = LocalDate.now().minusDays(30);
        }
        if (toDate == null) {
            toDate = LocalDate.now();
        }
        
        var stockOpt = stockService.findBySymbol(upperSymbol);
        if (stockOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Stock stock = stockOpt.get();
        stockHistoryService.fetchAndStoreHistoricalData(stock, fromDate, toDate);
        
        return ResponseEntity.ok("Historical data fetch initiated for " + upperSymbol);
    }

    /**
     * Convert StockHistory entity to DTO.
     */
    private HistoricalStockDataDto convertToDto(StockHistory stockHistory) {
        return new HistoricalStockDataDto(
                stockHistory.getStock().getSymbol(),
                stockHistory.getDate(),
                stockHistory.getOpenPrice(),
                stockHistory.getHighPrice(),
                stockHistory.getLowPrice(),
                stockHistory.getClosePrice(),
                stockHistory.getVolume()
        );
    }
}