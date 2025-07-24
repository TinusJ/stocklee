package com.tinusj.stocklee.repository;

import com.tinusj.stocklee.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for UserProfile entity.
 */
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {
    
    /**
     * Find user profile by username.
     */
    Optional<UserProfile> findByUsername(String username);
    
    /**
     * Find user profile by email.
     */
    Optional<UserProfile> findByEmail(String email);
    
    /**
     * Check if a user with the given username exists.
     */
    boolean existsByUsername(String username);
    
    /**
     * Check if a user with the given email exists.
     */
    boolean existsByEmail(String email);
}