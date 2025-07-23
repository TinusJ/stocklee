package com.tinusj.stocklee.web;

import com.tinusj.stocklee.dto.UserProfileDto;
import com.tinusj.stocklee.entity.UserProfile;
import com.tinusj.stocklee.service.UserProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserProfileWebController.class)
class UserProfileWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserProfileService userProfileService;

    private UserProfile testUserProfile;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testUserProfile = new UserProfile();
        testUserProfile.setId(testId);
        testUserProfile.setUsername("testuser");
        testUserProfile.setEmail("test@example.com");
        testUserProfile.setBalance(new BigDecimal("1000.00"));
    }

    @Test
    void showCreateForm_ShouldSetEditModeToFalse() throws Exception {
        mockMvc.perform(get("/user-profiles/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("user-profile/form"))
                .andExpect(model().attributeExists("userProfileDto"))
                .andExpect(model().attribute("editMode", false));
    }

    @Test
    void showEditForm_ShouldSetEditModeToTrue() throws Exception {
        when(userProfileService.findById(testId)).thenReturn(Optional.of(testUserProfile));

        mockMvc.perform(get("/user-profiles/{id}/edit", testId))
                .andExpect(status().isOk())
                .andExpect(view().name("user-profile/form"))
                .andExpect(model().attributeExists("userProfileDto"))
                .andExpect(model().attribute("editMode", true));
    }

    @Test
    void createUserProfile_WithValidationErrors_ShouldSetEditModeToFalse() throws Exception {
        mockMvc.perform(post("/user-profiles")
                .param("username", "") // Invalid - empty username
                .param("email", "invalid-email") // Invalid email
                .param("balance", "-100")) // Invalid - negative balance
                .andExpect(status().isOk())
                .andExpect(view().name("user-profile/form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attribute("editMode", false));
    }

    @Test
    void updateUserProfile_WithValidationErrors_ShouldSetEditModeToTrue() throws Exception {
        mockMvc.perform(post("/user-profiles/{id}", testId)
                .param("username", "") // Invalid - empty username
                .param("email", "invalid-email") // Invalid email
                .param("balance", "-100")) // Invalid - negative balance
                .andExpect(status().isOk())
                .andExpect(view().name("user-profile/form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attribute("editMode", true));
    }

    @Test
    void createUserProfile_WithValidData_ShouldRedirectToDetailsPage() throws Exception {
        UserProfile savedUserProfile = new UserProfile();
        savedUserProfile.setId(testId);
        savedUserProfile.setUsername("newuser");
        savedUserProfile.setEmail("newuser@example.com");
        savedUserProfile.setBalance(new BigDecimal("500.00"));

        when(userProfileService.save(any(UserProfile.class))).thenReturn(savedUserProfile);

        mockMvc.perform(post("/user-profiles")
                .param("username", "newuser")
                .param("email", "newuser@example.com")
                .param("balance", "500.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user-profiles/" + testId));
    }
}