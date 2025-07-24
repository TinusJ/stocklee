package com.tinusj.stocklee.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.tinusj.stocklee.config.TestSecurityConfig;
import com.tinusj.stocklee.repository.RoleRepository;
import com.tinusj.stocklee.repository.UserRepository;
import com.tinusj.stocklee.service.UserProfileService;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for AuthWebController authentication behavior.
 */
@WebMvcTest(AuthWebController.class)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class AuthWebControllerAuthenticationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RoleRepository roleRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private UserProfileService userProfileService;

    @Test
    void shouldShowLoginPageForUnauthenticatedUser() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(model().attributeExists("loginRequest"));
    }

    @Test  
    void shouldShowLoginPageWithErrorMessage() throws Exception {
        mockMvc.perform(get("/login?error=true"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(model().attribute("errorMessage", "Invalid email or password"));
    }

    @Test
    void shouldShowLoginPageWithLogoutMessage() throws Exception {
        mockMvc.perform(get("/login?logout=true"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(model().attribute("successMessage", "You have been logged out successfully"));
    }
}