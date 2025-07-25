package com.tinusj.stocklee.web;

import com.tinusj.stocklee.dto.UserProfileDto;
import com.tinusj.stocklee.entity.UserProfile;
import com.tinusj.stocklee.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

/**
 * Web controller for UserProfile entity Thymeleaf views.
 */
@Controller
@RequestMapping("/user-profiles")
@RequiredArgsConstructor
public class UserProfileWebController {

    private final UserProfileService userProfileService;

    /**
     * List all user profiles
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String listUserProfiles(Model model) {
        List<UserProfile> userProfiles = userProfileService.findAll();
        model.addAttribute("userProfiles", userProfiles);
        return "user-profile/list";
    }

    /**
     * Show user profile details
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String showUserProfile(@PathVariable UUID id, Model model) {
        UserProfile userProfile = userProfileService.findById(id)
                .orElseThrow(() -> new RuntimeException("User profile not found with id: " + id));
        model.addAttribute("userProfile", userProfile);
        return "user-profile/details";
    }

    /**
     * Show create user profile form
     */
    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String showCreateForm(Model model) {
        model.addAttribute("userProfileDto", new UserProfileDto());
        model.addAttribute("editMode", false);
        return "user-profile/form";
    }

    /**
     * Create new user profile
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String createUserProfile(@Valid @ModelAttribute UserProfileDto userProfileDto, 
                                   BindingResult result, 
                                   Model model, 
                                   RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("editMode", false);
            return "user-profile/form";
        }

        UserProfile userProfile = convertToEntity(userProfileDto);
        UserProfile savedUserProfile = userProfileService.save(userProfile);
        
        redirectAttributes.addFlashAttribute("successMessage", "User profile created successfully!");
        return "redirect:/user-profiles/" + savedUserProfile.getId();
    }

    /**
     * Show edit user profile form
     */
    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String showEditForm(@PathVariable UUID id, Model model) {
        UserProfile userProfile = userProfileService.findById(id)
                .orElseThrow(() -> new RuntimeException("User profile not found with id: " + id));
        
        UserProfileDto userProfileDto = convertToDto(userProfile);
        model.addAttribute("userProfileDto", userProfileDto);
        model.addAttribute("editMode", true);
        return "user-profile/form";
    }

    /**
     * Update user profile
     */
    @PostMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateUserProfile(@PathVariable UUID id, 
                                   @Valid @ModelAttribute UserProfileDto userProfileDto, 
                                   BindingResult result, 
                                   Model model, 
                                   RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("editMode", true);
            return "user-profile/form";
        }

        UserProfile userProfile = convertToEntity(userProfileDto);
        userProfile.setId(id);
        userProfileService.save(userProfile);
        
        redirectAttributes.addFlashAttribute("successMessage", "User profile updated successfully!");
        return "redirect:/user-profiles/" + id;
    }

    /**
     * Delete user profile
     */
    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteUserProfile(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        userProfileService.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "User profile deleted successfully!");
        return "redirect:/user-profiles";
    }

    private UserProfileDto convertToDto(UserProfile userProfile) {
        UserProfileDto dto = new UserProfileDto();
        dto.setId(userProfile.getId());
        dto.setUsername(userProfile.getUsername());
        dto.setEmail(userProfile.getEmail());
        dto.setBalance(userProfile.getBalance());
        dto.setCreatedAt(userProfile.getCreatedAt());
        dto.setUpdatedAt(userProfile.getUpdatedAt());
        return dto;
    }

    private UserProfile convertToEntity(UserProfileDto dto) {
        UserProfile userProfile = new UserProfile();
        userProfile.setUsername(dto.getUsername());
        userProfile.setEmail(dto.getEmail());
        userProfile.setBalance(dto.getBalance());
        return userProfile;
    }
}