package com.tinusj.stocklee.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

/**
 * DTO for user information response.
 */
@Data
@AllArgsConstructor
public class UserInfoResponse {
    private UUID id;
    private String username;
    private Set<String> roles;
}