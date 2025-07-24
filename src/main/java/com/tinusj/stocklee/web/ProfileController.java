package com.tinusj.stocklee.web;

import com.tinusj.stocklee.dto.UserProfileDto;
import com.tinusj.stocklee.entity.UserProfile;
import com.tinusj.stocklee.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/**
 * Controller for user profile management - allows users to edit their own profile only.
 */
@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserProfileService userProfileService;

    /**
     * Show the current user's profile for editing
     */
    @GetMapping
    public String showProfile(Authentication authentication, Model model) {
        String userEmail = authentication.getName();
        Optional<UserProfile> userOpt = userProfileService.findByEmail(userEmail);
        
        if (userOpt.isEmpty()) {
            model.addAttribute("errorMessage", "User profile not found. Please contact support.");
            return "error/general";
        }
        
        UserProfile userProfile = userOpt.get();
        UserProfileDto userProfileDto = convertToDto(userProfile);
        
        model.addAttribute("userProfileDto", userProfileDto);
        model.addAttribute("currentUser", userProfile);
        
        return "profile/edit";
    }

    /**
     * Update the current user's profile
     */
    @PostMapping
    public String updateProfile(Authentication authentication,
                               @Valid @ModelAttribute UserProfileDto userProfileDto,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        
        String userEmail = authentication.getName();
        Optional<UserProfile> userOpt = userProfileService.findByEmail(userEmail);
        
        if (userOpt.isEmpty()) {
            model.addAttribute("errorMessage", "User profile not found. Please contact support.");
            return "error/general";
        }
        
        if (result.hasErrors()) {
            model.addAttribute("currentUser", userOpt.get());
            return "profile/edit";
        }

        UserProfile userProfile = userOpt.get();
        
        // Only allow updating username and email (not balance - that's managed by trading)
        userProfile.setUsername(userProfileDto.getUsername());
        userProfile.setEmail(userProfileDto.getEmail());
        
        userProfileService.save(userProfile);
        
        redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        return "redirect:/profile";
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
}