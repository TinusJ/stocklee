package com.tinusj.stocklee.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for security configuration.
 * Tests that admin-only endpoints are properly secured for unauthenticated users.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // Test that unauthenticated users cannot access restricted pages
    @Test
    void unauthenticatedUser_ShouldBeUnauthorizedOrRedirected() throws Exception {
        mockMvc.perform(get("/user-profiles"))
                .andExpect(status().isUnauthorized());
        
        mockMvc.perform(get("/history-logs"))
                .andExpect(status().isUnauthorized());
        
        mockMvc.perform(get("/stock-history"))
                .andExpect(status().isUnauthorized());
        
        mockMvc.perform(get("/transaction-history"))
                .andExpect(status().isUnauthorized());
    }

    // Test that unauthenticated users cannot access restricted API endpoints
    @Test
    void unauthenticatedUser_ShouldGetUnauthorizedFromAPIEndpoints() throws Exception {
        mockMvc.perform(get("/api/user-profiles"))
                .andExpect(status().isUnauthorized());
        
        mockMvc.perform(get("/api/history-logs"))
                .andExpect(status().isUnauthorized());
        
        mockMvc.perform(get("/api/stock-histories"))
                .andExpect(status().isUnauthorized());
        
        mockMvc.perform(get("/api/transaction-histories"))
                .andExpect(status().isUnauthorized());
    }

    // Test that public endpoints are still accessible
    @Test
    void unauthenticatedUser_ShouldAccessPublicEndpoints() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
        
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk());
    }
}