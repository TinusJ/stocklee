package com.tinusj.stocklee.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Tracks the historical price changes of a stock with OHLCV data.
 */
@Entity
@Data
@NoArgsConstructor
public class StockHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Stock stock;

    @Column(nullable = false, scale = 2)
    private BigDecimal price;

    // OHLCV data for enhanced historical tracking
    @Column(scale = 2)
    private BigDecimal openPrice;

    @Column(scale = 2) 
    private BigDecimal highPrice;

    @Column(scale = 2)
    private BigDecimal lowPrice;

    @Column(scale = 2)
    private BigDecimal closePrice;

    private Long volume;

    // Date for daily historical data (separate from timestamp for intraday updates)
    private LocalDate date;

    private LocalDateTime timestamp;

    @PrePersist
    public void prePersist() {
        this.timestamp = LocalDateTime.now();
        if (this.date == null) {
            this.date = LocalDate.now();
        }
    }

    /**
     * Constructor for simple price updates (backward compatibility).
     */
    public StockHistory(Stock stock, BigDecimal price) {
        this.stock = stock;
        this.price = price;
        this.closePrice = price; // Use price as close price for backward compatibility
    }

    /**
     * Constructor for full OHLCV data.
     */
    public StockHistory(Stock stock, LocalDate date, BigDecimal openPrice, BigDecimal highPrice, 
                       BigDecimal lowPrice, BigDecimal closePrice, Long volume) {
        this.stock = stock;
        this.date = date;
        this.openPrice = openPrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.closePrice = closePrice;
        this.volume = volume;
        this.price = closePrice; // Set price to close price for compatibility
    }
}