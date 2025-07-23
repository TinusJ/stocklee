package com.tinusj.stocklee.entity;

import com.tinusj.stocklee.entity.enums.TransactionType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Tracks transactions (buy/sell) for stocks
 */
@Entity
@Table(name = "stock_transactions")
@Data
@NoArgsConstructor
public class StockTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalValue;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_profile_id", nullable = false)
    private UserProfile userProfile;

    @OneToMany(mappedBy = "stockTransaction", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TransactionHistory> transactionHistory;

    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
        
        // Calculate total value if not set
        if (this.totalValue == null && this.price != null && this.quantity != null) {
            this.totalValue = this.price.multiply(BigDecimal.valueOf(this.quantity));
        }
    }
}