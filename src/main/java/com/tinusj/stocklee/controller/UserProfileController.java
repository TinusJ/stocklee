package com.tinusj.stocklee.controller;

import com.tinusj.stocklee.entity.UserProfile;
import com.tinusj.stocklee.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for UserProfile entity operations.
 */
@RestController
@RequestMapping("/api/user-profiles")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    /**
     * Get all user profiles.
     */
    @GetMapping
    public ResponseEntity<List<UserProfile>> getAllUserProfiles() {
        List<UserProfile> userProfiles = userProfileService.findAll();
        return ResponseEntity.ok(userProfiles);
    }

    /**
     * Get user profile by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserProfile> getUserProfileById(@PathVariable UUID id) {
        return userProfileService.findById(id)
                .map(userProfile -> ResponseEntity.ok(userProfile))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new user profile.
     */
    @PostMapping
    public ResponseEntity<UserProfile> createUserProfile(@RequestBody UserProfile userProfile) {
        UserProfile savedUserProfile = userProfileService.save(userProfile);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUserProfile);
    }

    /**
     * Update an existing user profile.
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserProfile> updateUserProfile(@PathVariable UUID id, @RequestBody UserProfile userProfile) {
        if (!userProfileService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userProfile.setId(id);
        UserProfile updatedUserProfile = userProfileService.save(userProfile);
        return ResponseEntity.ok(updatedUserProfile);
    }

    /**
     * Delete user profile by ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserProfile(@PathVariable UUID id) {
        if (!userProfileService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userProfileService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get count of all user profiles.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> getUserProfileCount() {
        long count = userProfileService.count();
        return ResponseEntity.ok(count);
    }
}