package com.tinusj.stocklee.dto;

import lombok.Data;

/**
 * DTO for user signup request.
 */
@Data
public class SignupRequest {
    private String username;
    private String password;
}