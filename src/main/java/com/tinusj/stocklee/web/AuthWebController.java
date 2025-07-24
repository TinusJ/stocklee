package com.tinusj.stocklee.web;

import com.tinusj.stocklee.dto.LoginRequest;
import com.tinusj.stocklee.dto.SignupRequest;
import com.tinusj.stocklee.entity.Role;
import com.tinusj.stocklee.entity.User;
import com.tinusj.stocklee.entity.UserProfile;
import com.tinusj.stocklee.repository.RoleRepository;
import com.tinusj.stocklee.repository.UserRepository;
import com.tinusj.stocklee.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Web controller for handling authentication forms.
 */
@Controller
@RequiredArgsConstructor
public class AuthWebController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserProfileService userProfileService;

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "error", required = false) String error,
                               @RequestParam(value = "logout", required = false) String logout,
                               Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid email or password");
        }
        
        if (logout != null) {
            model.addAttribute("successMessage", "You have been logged out successfully");
        }
        
        return "auth/login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("signupRequest", new SignupRequest());
        return "auth/register";
    }

    @PostMapping("/users/create")
    public String registerUser(@Valid @ModelAttribute("signupRequest") SignupRequest signupRequest,
                              BindingResult result,
                              RedirectAttributes redirectAttributes) {
        
        // Check for validation errors
        if (result.hasErrors()) {
            return "auth/register";
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            result.rejectValue("email", "error.signupRequest", "Email is already in use");
            return "auth/register";
        }
        
        // Check if passwords match
        if (!signupRequest.getPassword().equals(signupRequest.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.signupRequest", "Passwords do not match");
            return "auth/register";
        }
        
        try {
            // Create new user for authentication
            User user = new User();
            user.setUsername(signupRequest.getFullName());
            user.setEmail(signupRequest.getEmail());
            user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
            
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("Error: Role ROLE_USER is not found."));
            user.setRoles(Set.of(userRole));
            
            userRepository.save(user);
            
            // Create corresponding UserProfile for business logic
            UserProfile userProfile = new UserProfile();
            userProfile.setUsername(signupRequest.getFullName());
            userProfile.setEmail(signupRequest.getEmail());
            userProfile.setBalance(BigDecimal.valueOf(10000.00)); // Starting balance of $10,000
            
            userProfileService.save(userProfile);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Registration successful! Please log in with your credentials. You start with $10,000 to invest.");
            return "redirect:/login";
            
        } catch (Exception e) {
            result.rejectValue("email", "error.signupRequest", "Registration failed. Please try again.");
            return "auth/register";
        }
    }
}