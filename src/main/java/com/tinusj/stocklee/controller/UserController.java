package com.tinusj.stocklee.controller;

import com.tinusj.stocklee.dto.UserInfoResponse;
import com.tinusj.stocklee.entity.User;
import com.tinusj.stocklee.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

/**
 * REST controller for user operations.
 */
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    /**
     * Get logged-in user's details.
     */
    @GetMapping("/user")
    public ResponseEntity<UserInfoResponse> getUserInfo(Authentication authentication) {
        String username = authentication.getName();
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserInfoResponse response = new UserInfoResponse(
            user.getId(),
            user.getUsername(),
            user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toSet())
        );

        return ResponseEntity.ok(response);
    }
}