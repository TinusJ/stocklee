package com.tinusj.stocklee.config;

import com.tinusj.stocklee.entity.*;
import com.tinusj.stocklee.repository.RoleRepository;
import com.tinusj.stocklee.repository.UserRepository;
import com.tinusj.stocklee.service.HistoryLogService;
import com.tinusj.stocklee.service.OwnedStockService;
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
    private final OwnedStockService ownedStockService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        initializeRoles();
        initializeAdminUser();
        initializeRegularUser();
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
        if (userRepository.findByEmail("admin@stocklee.com").isPresent()) {
            log.info("Admin user already exists, skipping initialization");
            return;
        }

        // Create default admin user
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new RuntimeException("ROLE_ADMIN not found"));

        User adminUser = new User();
        adminUser.setUsername("Administrator");
        adminUser.setEmail("admin@stocklee.com");
        adminUser.setPassword(passwordEncoder.encode("admin"));
        adminUser.setRoles(Set.of(adminRole));

        userRepository.save(adminUser);
        
        // Create corresponding UserProfile for admin user
        UserProfile adminProfile = new UserProfile();
        adminProfile.setUsername("Administrator");
        adminProfile.setEmail("admin@stocklee.com");
        adminProfile.setBalance(BigDecimal.valueOf(50000.00)); // Admin starts with $50,000
        
        userProfileService.save(adminProfile);
        
        log.info("Created default admin user with email 'admin@stocklee.com' and password 'admin'");
    }

    private void initializeRegularUser() {
        // Check if regular user already exists
        if (userRepository.findByEmail("user@stocklee.com").isPresent()) {
            log.info("Regular user already exists, skipping initialization");
            return;
        }

        // Create default regular user
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("ROLE_USER not found"));

        User regularUser = new User();
        regularUser.setUsername("RegularUser");
        regularUser.setEmail("user@stocklee.com");
        regularUser.setPassword(passwordEncoder.encode("user"));
        regularUser.setRoles(Set.of(userRole));

        userRepository.save(regularUser);
        
        // Create corresponding UserProfile for regular user
        UserProfile regularProfile = new UserProfile();
        regularProfile.setUsername("RegularUser");
        regularProfile.setEmail("user@stocklee.com");
        regularProfile.setBalance(BigDecimal.valueOf(10000.00)); // User starts with $10,000
        
        userProfileService.save(regularProfile);
        
        log.info("Created default regular user with email 'user@stocklee.com' and password 'user'");
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
        UserProfile adminUser = userProfileService.findByEmail("admin@stocklee.com").orElse(null);
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

        // Create sample transactions and owned stocks
        StockTransaction buyTransaction = new StockTransaction();
        buyTransaction.setUser(guestUser);
        buyTransaction.setStock(appleStock);
        buyTransaction.setTransactionType(StockTransaction.TransactionType.BUY);
        buyTransaction.setQuantity(BigDecimal.valueOf(10));
        buyTransaction.setPrice(BigDecimal.valueOf(145.00));
        buyTransaction.setTotalValue(BigDecimal.valueOf(1450.00));
        buyTransaction.setTimestamp(LocalDateTime.now().minusDays(1));
        stockTransactionService.save(buyTransaction);

        // Create corresponding owned stock
        ownedStockService.addShares(guestUser, appleStock, BigDecimal.valueOf(10), BigDecimal.valueOf(145.00));

        // Create another owned stock for Microsoft
        StockTransaction buyTransaction2 = new StockTransaction();
        buyTransaction2.setUser(guestUser);
        buyTransaction2.setStock(microsoftStock);
        buyTransaction2.setTransactionType(StockTransaction.TransactionType.BUY);
        buyTransaction2.setQuantity(BigDecimal.valueOf(5));
        buyTransaction2.setPrice(BigDecimal.valueOf(295.00));
        buyTransaction2.setTotalValue(BigDecimal.valueOf(1475.00));
        buyTransaction2.setTimestamp(LocalDateTime.now().minusDays(2));
        stockTransactionService.save(buyTransaction2);

        // Create corresponding owned stock
        ownedStockService.addShares(guestUser, microsoftStock, BigDecimal.valueOf(5), BigDecimal.valueOf(295.00));

        // Update guest user's balance to reflect purchases
        BigDecimal currentBalance = guestUser.getBalance();
        BigDecimal newBalance = currentBalance.subtract(BigDecimal.valueOf(1450.00 + 1475.00));
        guestUser.setBalance(newBalance);
        userProfileService.save(guestUser);

        // Create sample history logs
        HistoryLog historyLog = new HistoryLog();
        historyLog.setUser(guestUser);
        historyLog.setAction("Purchased 10 shares of AAPL at $145.00 per share");
        historyLog.setTimestamp(LocalDateTime.now().minusDays(1));
        historyLogService.save(historyLog);

        HistoryLog historyLog2 = new HistoryLog();
        historyLog2.setUser(guestUser);
        historyLog2.setAction("Purchased 5 shares of MSFT at $295.00 per share");
        historyLog2.setTimestamp(LocalDateTime.now().minusDays(2));
        historyLogService.save(historyLog2);

        // Add some owned stocks for admin user too (if exists)
        if (adminUser != null) {
            // Create some Tesla stock
            Stock teslaStock = new Stock();
            teslaStock.setSymbol("TSLA");
            teslaStock.setName("Tesla Inc.");
            teslaStock.setDescription("Electric vehicle and clean energy company");
            teslaStock.setCurrentPrice(BigDecimal.valueOf(200.00));
            teslaStock.setMarket(Stock.MarketType.NASDAQ);
            stockService.save(teslaStock);

            // Add shares for admin
            ownedStockService.addShares(adminUser, teslaStock, BigDecimal.valueOf(25), BigDecimal.valueOf(190.00));
            ownedStockService.addShares(adminUser, appleStock, BigDecimal.valueOf(15), BigDecimal.valueOf(148.00));

            // Create transactions for admin
            StockTransaction adminTransaction1 = new StockTransaction();
            adminTransaction1.setUser(adminUser);
            adminTransaction1.setStock(teslaStock);
            adminTransaction1.setTransactionType(StockTransaction.TransactionType.BUY);
            adminTransaction1.setQuantity(BigDecimal.valueOf(25));
            adminTransaction1.setPrice(BigDecimal.valueOf(190.00));
            adminTransaction1.setTotalValue(BigDecimal.valueOf(4750.00));
            adminTransaction1.setTimestamp(LocalDateTime.now().minusDays(3));
            stockTransactionService.save(adminTransaction1);

            StockTransaction adminTransaction2 = new StockTransaction();
            adminTransaction2.setUser(adminUser);
            adminTransaction2.setStock(appleStock);
            adminTransaction2.setTransactionType(StockTransaction.TransactionType.BUY);
            adminTransaction2.setQuantity(BigDecimal.valueOf(15));
            adminTransaction2.setPrice(BigDecimal.valueOf(148.00));
            adminTransaction2.setTotalValue(BigDecimal.valueOf(2220.00));
            adminTransaction2.setTimestamp(LocalDateTime.now().minusDays(4));
            stockTransactionService.save(adminTransaction2);

            // Update admin balance
            BigDecimal adminBalance = adminUser.getBalance();
            BigDecimal newAdminBalance = adminBalance.subtract(BigDecimal.valueOf(4750.00 + 2220.00));
            adminUser.setBalance(newAdminBalance);
            userProfileService.save(adminUser);

            log.info("Added owned stocks for admin user as well");
        }

        log.info("Created sample data: 3 stocks, 4 transactions, 4 owned stocks, 2 history logs");
    }
}