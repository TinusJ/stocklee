package com.tinusj.stocklee.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO for API response messages.
 */
@Data
@AllArgsConstructor
public class MessageResponse {
    private String message;
}