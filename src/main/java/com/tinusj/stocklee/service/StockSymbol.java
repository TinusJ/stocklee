package com.tinusj.stocklee.service;

/**
 * Enum for commonly traded stock symbols.
 */
public enum StockSymbol {
    AAPL("Apple Inc."),
    MSFT("Microsoft Corporation"),
    GOOGL("Alphabet Inc."),
    AMZN("Amazon.com Inc."),
    TSLA("Tesla Inc."),
    META("Meta Platforms Inc."),
    NVDA("NVIDIA Corporation"),
    JPM("JPMorgan Chase & Co."),
    JNJ("Johnson & Johnson"),
    V("Visa Inc."),
    PG("Procter & Gamble Co."),
    HD("Home Depot Inc."),
    UNH("UnitedHealth Group Inc."),
    BAC("Bank of America Corp."),
    MA("Mastercard Inc."),
    DIS("Walt Disney Co."),
    ADBE("Adobe Inc."),
    NFLX("Netflix Inc."),
    CRM("Salesforce Inc."),
    XOM("Exxon Mobil Corp.");

    private final String companyName;

    StockSymbol(String companyName) {
        this.companyName = companyName;
    }

    public String getSymbol() {
        return name();
    }

    public String getCompanyName() {
        return companyName;
    }

    /**
     * Check if a symbol is in the enum.
     */
    public static boolean contains(String symbol) {
        if (symbol == null) return false;
        try {
            valueOf(symbol.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Get company name for a symbol, or return the symbol if not found.
     */
    public static String getCompanyNameForSymbol(String symbol) {
        if (symbol == null) return null;
        try {
            return valueOf(symbol.toUpperCase()).getCompanyName();
        } catch (IllegalArgumentException e) {
            return symbol + " Corporation";
        }
    }
}