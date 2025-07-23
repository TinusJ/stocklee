package com.tinusj.stocklee.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Tracks stocks currently owned by a user
 */
@Entity
@Table(name = "owned_stocks", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_profile_id", "stock_id"})
})
@Data
@NoArgsConstructor
public class OwnedStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal averagePrice;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalValue;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_profile_id", nullable = false)
    private UserProfile userProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @PrePersist
    @PreUpdate
    protected void calculateTotalValue() {
        // Calculate total value if not set
        if (this.totalValue == null && this.averagePrice != null && this.quantity != null) {
            this.totalValue = this.averagePrice.multiply(BigDecimal.valueOf(this.quantity));
        }
    }
}