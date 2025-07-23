package com.tinusj.stocklee.entity.enums;

/**
 * Represents different market types where stocks are traded
 */
public enum MarketType {
    NYSE("New York Stock Exchange"),
    NASDAQ("NASDAQ Stock Market"),
    AMEX("American Stock Exchange"),
    OTC("Over-the-Counter"),
    LSE("London Stock Exchange"),
    TSE("Tokyo Stock Exchange"),
    OTHER("Other Market");

    private final String description;

    MarketType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}