package com.tinusj.stocklee.service;

import com.tinusj.stocklee.entity.UserProfile;
import com.tinusj.stocklee.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service layer for UserProfile entity operations.
 */
@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;

    /**
     * Find all user profiles.
     */
    public List<UserProfile> findAll() {
        return userProfileRepository.findAll();
    }

    /**
     * Find user profile by ID.
     */
    public Optional<UserProfile> findById(UUID id) {
        return userProfileRepository.findById(id);
    }

    /**
     * Save or update a user profile.
     */
    public UserProfile save(UserProfile userProfile) {
        return userProfileRepository.save(userProfile);
    }

    /**
     * Delete user profile by ID.
     */
    public void deleteById(UUID id) {
        userProfileRepository.deleteById(id);
    }

    /**
     * Check if user profile exists by ID.
     */
    public boolean existsById(UUID id) {
        return userProfileRepository.existsById(id);
    }

    /**
     * Get count of all user profiles.
     */
    public long count() {
        return userProfileRepository.count();
    }
}