package com.tinusj.stocklee.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a transaction (buy/sell) for a stock.
 */
@Entity
@Data
@NoArgsConstructor
public class StockTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Stock stock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private UserProfile user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;

    @Column(nullable = false, scale = 4)
    private BigDecimal quantity;

    @Column(nullable = false, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, scale = 2)
    private BigDecimal totalValue;

    private LocalDateTime timestamp;

    @PrePersist
    public void prePersist() {
        this.timestamp = LocalDateTime.now();
    }

    public enum TransactionType {
        BUY, SELL
    }
}