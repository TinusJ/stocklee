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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
     */
    @GetMapping
    public String listTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String transactionType,
            Model model) {
        
        // Create pageable with sorting
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        // Get all transactions for now (we can add filtering later)
        List<StockTransaction> transactions = stockTransactionService.findAll();
        
        // Filter by user if specified
        if (userId != null) {
            transactions = transactions.stream()
                .filter(t -> t.getUser().getId().equals(userId))
                .toList();
        }
        
        // Filter by transaction type if specified
        if (transactionType != null && !transactionType.isEmpty()) {
            transactions = transactions.stream()
                .filter(t -> t.getTransactionType().name().equalsIgnoreCase(transactionType))
                .toList();
        }
        
        // Get all users for filter dropdown
        List<UserProfile> users = userProfileService.findAll();
        
        model.addAttribute("transactions", transactions);
        model.addAttribute("users", users);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("selectedUserId", userId);
        model.addAttribute("selectedTransactionType", transactionType);
        model.addAttribute("pageTitle", "Transactions");
        
        return "transaction/list";
    }

    /**
     * Show transaction details.
     */
    @GetMapping("/{id}")
    public String showTransaction(@PathVariable UUID id, Model model) {
        StockTransaction transaction = stockTransactionService.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));
        
        model.addAttribute("transaction", transaction);
        model.addAttribute("pageTitle", "Transaction Details");
        
        return "transaction/details";
    }
}