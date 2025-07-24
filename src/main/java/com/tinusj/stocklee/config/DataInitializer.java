package com.tinusj.stocklee.config;

import com.tinusj.stocklee.entity.*;
import com.tinusj.stocklee.repository.RoleRepository;
import com.tinusj.stocklee.repository.UserRepository;
import com.tinusj.stocklee.service.HistoryLogService;
import com.tinusj.stocklee.service.StockService;
import com.tinusj.stocklee.service.StockTransactionService;
import com.tinusj.stocklee.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Initializes default data when the application starts.
 */
@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserProfileService userProfileService;
    private final StockService stockService;
    private final StockTransactionService stockTransactionService;
    private final HistoryLogService historyLogService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        initializeRoles();
        initializeAdminUser();
        initializeGuestUser();
        initializeSampleData();
    }

    private void initializeRoles() {
        // Create ROLE_USER if it doesn't exist
        if (roleRepository.findByName("ROLE_USER").isEmpty()) {
            Role userRole = new Role("ROLE_USER");
            roleRepository.save(userRole);
            log.info("Created ROLE_USER");
        }

        // Create ROLE_ADMIN if it doesn't exist
        if (roleRepository.findByName("ROLE_ADMIN").isEmpty()) {
            Role adminRole = new Role("ROLE_ADMIN");
            roleRepository.save(adminRole);
            log.info("Created ROLE_ADMIN");
        }
    }

    private void initializeAdminUser() {
        // Check if admin user already exists
        if (userRepository.findByUsername("admin").isPresent()) {
            log.info("Admin user already exists, skipping initialization");
            return;
        }

        // Create default admin user
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new RuntimeException("ROLE_ADMIN not found"));

        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setPassword(passwordEncoder.encode("admin"));
        adminUser.setRoles(Set.of(adminRole));

        userRepository.save(adminUser);
        log.info("Created default admin user with username 'admin' and password 'admin'");
    }

    private void initializeGuestUser() {
        // Check if Guest user already exists
        if (userProfileService.findByUsername("Guest").isPresent()) {
            log.info("Guest user already exists, skipping initialization");
            return;
        }

        // Create Guest user
        UserProfile guestUser = new UserProfile();
        guestUser.setUsername("Guest");
        guestUser.setEmail("guest@stocklee.com");
        guestUser.setBalance(BigDecimal.valueOf(10000.00)); // Starting balance of $10,000

        userProfileService.save(guestUser);
        log.info("Created Guest user with starting balance of $10,000");
    }

    private void initializeSampleData() {
        // Only add sample data if none exists
        if (stockService.count() > 0) {
            log.info("Sample data already exists, skipping initialization");
            return;
        }

        UserProfile guestUser = userProfileService.findByUsername("Guest").orElse(null);
        if (guestUser == null) {
            log.warn("Guest user not found, skipping sample data initialization");
            return;
        }

        // Create sample stocks
        Stock appleStock = new Stock();
        appleStock.setSymbol("AAPL");
        appleStock.setName("Apple Inc.");
        appleStock.setDescription("Technology company");
        appleStock.setCurrentPrice(BigDecimal.valueOf(150.00));
        appleStock.setMarket(Stock.MarketType.NASDAQ);
        stockService.save(appleStock);

        Stock microsoftStock = new Stock();
        microsoftStock.setSymbol("MSFT");
        microsoftStock.setName("Microsoft Corporation");
        microsoftStock.setDescription("Technology company");
        microsoftStock.setCurrentPrice(BigDecimal.valueOf(300.00));
        microsoftStock.setMarket(Stock.MarketType.NASDAQ);
        stockService.save(microsoftStock);

        // Create sample transactions
        StockTransaction buyTransaction = new StockTransaction();
        buyTransaction.setUser(guestUser);
        buyTransaction.setStock(appleStock);
        buyTransaction.setTransactionType(StockTransaction.TransactionType.BUY);
        buyTransaction.setQuantity(BigDecimal.valueOf(10));
        buyTransaction.setPrice(BigDecimal.valueOf(145.00));
        buyTransaction.setTotalValue(BigDecimal.valueOf(1450.00));
        buyTransaction.setTimestamp(LocalDateTime.now().minusDays(1));
        stockTransactionService.save(buyTransaction);

        // Create sample history log
        HistoryLog historyLog = new HistoryLog();
        historyLog.setUser(guestUser);
        historyLog.setAction("Purchased 10 shares of AAPL at $145.00 per share");
        historyLog.setTimestamp(LocalDateTime.now().minusDays(1));
        historyLogService.save(historyLog);

        log.info("Created sample data: 2 stocks, 1 transaction, 1 history log");
    }
}