package com.tinusj.stocklee.config;

import com.tinusj.stocklee.entity.UserProfile;
import com.tinusj.stocklee.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Initializes default data when the application starts.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserProfileService userProfileService;

    @Override
    public void run(String... args) throws Exception {
        initializeGuestUser();
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
}