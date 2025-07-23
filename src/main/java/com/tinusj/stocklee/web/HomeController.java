package com.tinusj.stocklee.web;

import com.tinusj.stocklee.dto.BuyStockDto;
import com.tinusj.stocklee.dto.PortfolioSummaryDto;
import com.tinusj.stocklee.dto.SellStockDto;
import com.tinusj.stocklee.entity.UserProfile;
import com.tinusj.stocklee.service.TradingService;
import com.tinusj.stocklee.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Home page controller.
 */
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final UserProfileService userProfileService;
    private final TradingService tradingService;

    @GetMapping("/")
    public String home(@RequestParam(required = false) UUID userId, Model model) {
        List<UserProfile> users = userProfileService.findAll();
        model.addAttribute("users", users);
        
        if (userId != null) {
            Optional<UserProfile> userOpt = userProfileService.findById(userId);
            if (userOpt.isPresent()) {
                UserProfile user = userOpt.get();
                PortfolioSummaryDto portfolio = tradingService.getPortfolioSummary(user);
                
                model.addAttribute("selectedUser", user);
                model.addAttribute("portfolio", portfolio);
                model.addAttribute("buyStockDto", new BuyStockDto());
                model.addAttribute("sellStockDto", new SellStockDto());
                return "dashboard";
            }
        }
        
        // If no user selected or user not found, show user selection
        return "index";
    }

    @PostMapping("/buy-stock")
    public String buyStock(@RequestParam UUID userId,
                          @Valid @ModelAttribute BuyStockDto buyStockDto,
                          BindingResult result,
                          RedirectAttributes redirectAttributes) {
        
        Optional<UserProfile> userOpt = userProfileService.findById(userId);
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found");
            return "redirect:/";
        }

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid buy request: " + result.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/?userId=" + userId;
        }

        String resultMessage = tradingService.buyStock(userOpt.get(), buyStockDto);
        
        if (resultMessage.startsWith("Successfully")) {
            redirectAttributes.addFlashAttribute("successMessage", resultMessage);
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", resultMessage);
        }
        
        return "redirect:/?userId=" + userId;
    }

    @PostMapping("/sell-stock")
    public String sellStock(@RequestParam UUID userId,
                           @Valid @ModelAttribute SellStockDto sellStockDto,
                           BindingResult result,
                           RedirectAttributes redirectAttributes) {
        
        Optional<UserProfile> userOpt = userProfileService.findById(userId);
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found");
            return "redirect:/";
        }

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid sell request: " + result.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/?userId=" + userId;
        }

        String resultMessage = tradingService.sellStock(userOpt.get(), sellStockDto);
        
        if (resultMessage.startsWith("Successfully")) {
            redirectAttributes.addFlashAttribute("successMessage", resultMessage);
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", resultMessage);
        }
        
        return "redirect:/?userId=" + userId;
    }
}