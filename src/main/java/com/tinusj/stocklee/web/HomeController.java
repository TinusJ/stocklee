package com.tinusj.stocklee.web;

import com.tinusj.stocklee.dto.BuyStockDto;
import com.tinusj.stocklee.dto.PortfolioSummaryDto;
import com.tinusj.stocklee.dto.SellStockDto;
import com.tinusj.stocklee.entity.UserProfile;
import com.tinusj.stocklee.service.TradingService;
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
 * Home page controller.
 */
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final UserProfileService userProfileService;
    private final TradingService tradingService;

    @GetMapping("/")
    public String home(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String userEmail = authentication.getName(); // This will be the email since we set it as username in UserDetailsService
        Optional<UserProfile> userOpt = userProfileService.findByEmail(userEmail);
        
        if (userOpt.isEmpty()) {
            model.addAttribute("errorMessage", "User profile not found. Please contact support.");
            return "error/general";
        }
        
        UserProfile user = userOpt.get();
        PortfolioSummaryDto portfolio = tradingService.getPortfolioSummary(user);
        
        model.addAttribute("currentUser", user);
        model.addAttribute("portfolio", portfolio);
        model.addAttribute("buyStockDto", new BuyStockDto());
        model.addAttribute("sellStockDto", new SellStockDto());
        
        return "dashboard";
    }

    @PostMapping("/buy-stock")
    public String buyStock(Authentication authentication,
                          @Valid @ModelAttribute BuyStockDto buyStockDto,
                          BindingResult result,
                          RedirectAttributes redirectAttributes) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String userEmail = authentication.getName();
        Optional<UserProfile> userOpt = userProfileService.findByEmail(userEmail);
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "User profile not found");
            return "redirect:/dashboard";
        }

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid buy request: " + result.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/dashboard";
        }

        String resultMessage = tradingService.buyStock(userOpt.get(), buyStockDto);
        
        if (resultMessage.startsWith("Successfully")) {
            redirectAttributes.addFlashAttribute("successMessage", resultMessage);
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", resultMessage);
        }
        
        return "redirect:/dashboard";
    }

    @PostMapping("/sell-stock")
    public String sellStock(Authentication authentication,
                           @Valid @ModelAttribute SellStockDto sellStockDto,
                           BindingResult result,
                           RedirectAttributes redirectAttributes) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String userEmail = authentication.getName();
        Optional<UserProfile> userOpt = userProfileService.findByEmail(userEmail);
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "User profile not found");
            return "redirect:/dashboard";
        }

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid sell request: " + result.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/dashboard";
        }

        String resultMessage = tradingService.sellStock(userOpt.get(), sellStockDto);
        
        if (resultMessage.startsWith("Successfully")) {
            redirectAttributes.addFlashAttribute("successMessage", resultMessage);
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", resultMessage);
        }
        
        return "redirect:/dashboard";
    }
}