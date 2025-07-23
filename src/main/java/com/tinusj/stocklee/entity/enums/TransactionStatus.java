package com.tinusj.stocklee.entity.enums;

/**
 * Represents the status of a stock transaction
 */
public enum TransactionStatus {
    PENDING("Transaction Pending"),
    PROCESSING("Transaction Processing"),
    COMPLETED("Transaction Completed"),
    CANCELLED("Transaction Cancelled"),
    FAILED("Transaction Failed"),
    REJECTED("Transaction Rejected");

    private final String description;

    TransactionStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}