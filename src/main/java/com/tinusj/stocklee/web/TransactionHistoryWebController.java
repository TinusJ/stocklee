package com.tinusj.stocklee.web;

import com.tinusj.stocklee.entity.StockTransaction;
import com.tinusj.stocklee.entity.TransactionHistory;
import com.tinusj.stocklee.entity.UserProfile;
import com.tinusj.stocklee.service.StockTransactionService;
import com.tinusj.stocklee.service.TransactionHistoryService;
import com.tinusj.stocklee.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Web controller for Transaction History pages.
 */
@Controller
@RequestMapping("/transaction-history")
@RequiredArgsConstructor
public class TransactionHistoryWebController {

    private final TransactionHistoryService transactionHistoryService;
    private final StockTransactionService stockTransactionService;
    private final UserProfileService userProfileService;

    /**
     * List all transaction history with pagination and filtering.
     */
    @GetMapping
    public String listTransactionHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String symbol,
            @RequestParam(required = false) String status,
            Model model) {
        
        // Create pageable with sorting
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        // Get all transaction history
        List<TransactionHistory> transactionHistories = transactionHistoryService.findAll();
        
        // Filter by user if specified
        if (userId != null) {
            transactionHistories = transactionHistories.stream()
                .filter(th -> th.getTransaction().getUser().getId().equals(userId))
                .toList();
        }
        
        // Filter by stock symbol if specified
        if (symbol != null && !symbol.trim().isEmpty()) {
            transactionHistories = transactionHistories.stream()
                .filter(th -> th.getTransaction().getStock().getSymbol().equalsIgnoreCase(symbol.trim()))
                .toList();
        }
        
        // Filter by status if specified
        if (status != null && !status.isEmpty()) {
            transactionHistories = transactionHistories.stream()
                .filter(th -> th.getStatus().name().equalsIgnoreCase(status))
                .toList();
        }
        
        // Sort manually for now
        transactionHistories.sort((th1, th2) -> {
            if (sortBy.equals("timestamp")) {
                int result = th1.getTimestamp().compareTo(th2.getTimestamp());
                return direction == Sort.Direction.DESC ? -result : result;
            }
            return 0;
        });
        
        // Get all users for filter dropdown
        List<UserProfile> users = userProfileService.findAll();
        
        model.addAttribute("transactionHistories", transactionHistories);
        model.addAttribute("users", users);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("selectedUserId", userId);
        model.addAttribute("selectedSymbol", symbol);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("pageTitle", "Transaction History");
        
        return "transaction-history/list";
    }

    /**
     * Show transaction history details.
     */
    @GetMapping("/{id}")
    public String showTransactionHistory(@PathVariable UUID id, Model model) {
        TransactionHistory transactionHistory = transactionHistoryService.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction history not found with id: " + id));
        
        model.addAttribute("transactionHistory", transactionHistory);
        model.addAttribute("pageTitle", "Transaction History Details");
        
        return "transaction-history/details";
    }

    /**
     * Show detailed transaction history for a specific transaction.
     */
    @GetMapping("/transaction/{transactionId}")
    public String showTransactionHistoryByTransaction(@PathVariable UUID transactionId, Model model) {
        StockTransaction transaction = stockTransactionService.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + transactionId));
        
        // Get history for this transaction
        List<TransactionHistory> transactionHistories = transactionHistoryService.findAll().stream()
            .filter(th -> th.getTransaction().getId().equals(transactionId))
            .sorted((th1, th2) -> th2.getTimestamp().compareTo(th1.getTimestamp())) // Latest first
            .toList();
        
        model.addAttribute("transaction", transaction);
        model.addAttribute("transactionHistories", transactionHistories);
        model.addAttribute("pageTitle", "Transaction History - " + transaction.getStock().getSymbol());
        
        return "transaction-history/by-transaction";
    }
}