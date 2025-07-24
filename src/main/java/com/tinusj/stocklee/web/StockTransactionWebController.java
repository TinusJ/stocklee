package com.tinusj.stocklee.web;

import com.tinusj.stocklee.entity.StockTransaction;
import com.tinusj.stocklee.entity.UserProfile;
import com.tinusj.stocklee.service.StockTransactionService;
import com.tinusj.stocklee.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Web controller for Stock Transaction pages.
 */
@Controller
@RequestMapping("/stock-transactions")
@RequiredArgsConstructor
public class StockTransactionWebController {

    private final StockTransactionService stockTransactionService;
    private final UserProfileService userProfileService;

    /**
     * List all stock transactions with pagination and filtering.
     * Regular users see only their own transactions, admins see all.
     */
    @GetMapping
    public String listTransactions(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String transactionType,
            Model model) {
        
        String userEmail = authentication.getName();
        Optional<UserProfile> currentUserOpt = userProfileService.findByEmail(userEmail);
        
        if (currentUserOpt.isEmpty()) {
            model.addAttribute("errorMessage", "User profile not found");
            return "error/general";
        }
        
        UserProfile currentUser = currentUserOpt.get();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        
        List<StockTransaction> transactions;
        
        if (isAdmin) {
            // Admins can see all transactions or filter by user
            if (userId != null) {
                Optional<UserProfile> selectedUserOpt = userProfileService.findById(userId);
                if (selectedUserOpt.isPresent()) {
                    transactions = stockTransactionService.findByUser(selectedUserOpt.get());
                } else {
                    transactions = stockTransactionService.findAll();
                }
            } else {
                transactions = stockTransactionService.findAll();
            }
            // Get all users for admin filter dropdown
            List<UserProfile> users = userProfileService.findAll();
            model.addAttribute("users", users);
        } else {
            // Regular users see only their own transactions
            transactions = stockTransactionService.findByUser(currentUser);
        }
        
        // Filter by transaction type if specified
        if (transactionType != null && !transactionType.isEmpty()) {
            transactions = transactions.stream()
                .filter(t -> t.getTransactionType().name().equalsIgnoreCase(transactionType))
                .toList();
        }
        
        model.addAttribute("transactions", transactions);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("selectedUserId", userId);
        model.addAttribute("selectedTransactionType", transactionType);
        model.addAttribute("pageTitle", "Transactions");
        model.addAttribute("isAdmin", isAdmin);
        
        return "transaction/list";
    }

    /**
     * Show transaction details.
     * Users can only see their own transactions, admins can see all.
     */
    @GetMapping("/{id}")
    public String showTransaction(@PathVariable UUID id, Authentication authentication, Model model) {
        String userEmail = authentication.getName();
        Optional<UserProfile> currentUserOpt = userProfileService.findByEmail(userEmail);
        
        if (currentUserOpt.isEmpty()) {
            model.addAttribute("errorMessage", "User profile not found");
            return "error/general";
        }
        
        UserProfile currentUser = currentUserOpt.get();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        
        StockTransaction transaction = stockTransactionService.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));
        
        // Check if user can access this transaction
        if (!isAdmin && !transaction.getUser().getId().equals(currentUser.getId())) {
            model.addAttribute("errorMessage", "Access denied: You can only view your own transactions");
            return "error/general";
        }
        
        model.addAttribute("transaction", transaction);
        model.addAttribute("pageTitle", "Transaction Details");
        
        return "transaction/details";
    }
}