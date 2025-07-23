package com.tinusj.stocklee.entity.enums;

/**
 * Represents the type of stock transaction
 */
public enum TransactionType {
    BUY("Buy Order"),
    SELL("Sell Order");

    private final String description;

    TransactionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}