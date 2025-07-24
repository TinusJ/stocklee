package com.tinusj.stocklee.controller;

import com.tinusj.stocklee.dto.MessageResponse;
import com.tinusj.stocklee.dto.UserInfoResponse;
import com.tinusj.stocklee.entity.User;
import com.tinusj.stocklee.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for admin operations.
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;

    /**
     * Get all users (admin only).
     */
    @GetMapping("/users")
    public ResponseEntity<List<UserInfoResponse>> getAllUsers() {
        List<User> users = userRepository.findAll();
        
        List<UserInfoResponse> userResponses = users.stream()
                .map(user -> new UserInfoResponse(
                    user.getId(),
                    user.getUsername(),
                    user.getRoles().stream()
                        .map(role -> role.getName())
                        .collect(Collectors.toSet())
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(userResponses);
    }

    /**
     * Delete a user by ID (admin only).
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable UUID id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        userRepository.deleteById(id);
        return ResponseEntity.ok(new MessageResponse("User deleted successfully!"));
    }
}