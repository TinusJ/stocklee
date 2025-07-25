package com.tinusj.stocklee.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Tracks stocks currently owned by a user.
 */
@Entity
@Data
@NoArgsConstructor
public class OwnedStock {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private UserProfile user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Stock stock;

    @Column(nullable = false, scale = 4)
    private BigDecimal quantity;

    @Column(nullable = false, scale = 2)
    private BigDecimal averagePrice;

    @Column(nullable = false, scale = 2)
    private BigDecimal totalValue;
    
    // Transient properties for calculations
    @Transient
    private BigDecimal currentPrice;
    
    @Transient
    private BigDecimal currentValue;
    
    @Transient
    private BigDecimal profitLoss;
    
    @Transient
    private BigDecimal profitLossPercentage;
}