package com.tinusj.stocklee.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Application-specific configuration properties for the Stock Trading Application.
 */
@Configuration
@ConfigurationProperties(prefix = "stocklee")
public class ApplicationConfig {

    private final Scheduler scheduler = new Scheduler();

    public Scheduler getScheduler() {
        return scheduler;
    }

    public static class Scheduler {
        private String marketDataFetchCron = "0 */30 * * * *";

        public String getMarketDataFetchCron() {
            return marketDataFetchCron;
        }

        public void setMarketDataFetchCron(String marketDataFetchCron) {
            this.marketDataFetchCron = marketDataFetchCron;
        }
    }
}