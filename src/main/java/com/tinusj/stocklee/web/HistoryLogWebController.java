package com.tinusj.stocklee.web;

import com.tinusj.stocklee.entity.HistoryLog;
import com.tinusj.stocklee.entity.UserProfile;
import com.tinusj.stocklee.service.HistoryLogService;
import com.tinusj.stocklee.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Web controller for History Logs pages.
 */
@Controller
@RequestMapping("/history-logs")
@RequiredArgsConstructor
public class HistoryLogWebController {

    private final HistoryLogService historyLogService;
    private final UserProfileService userProfileService;

    /**
     * List all history logs with pagination and filtering.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String listHistoryLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) UUID userId,
            Model model) {
        
        // Create pageable with sorting
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        // Get all history logs
        List<HistoryLog> historyLogs = historyLogService.findAll();
        
        // Filter by user if specified
        if (userId != null) {
            historyLogs = historyLogs.stream()
                .filter(log -> log.getUser().getId().equals(userId))
                .toList();
        }
        
        // Sort manually for now (we can improve this with repository methods later)
        historyLogs.sort((log1, log2) -> {
            if (sortBy.equals("timestamp")) {
                int result = log1.getTimestamp().compareTo(log2.getTimestamp());
                return direction == Sort.Direction.DESC ? -result : result;
            }
            return 0;
        });
        
        // Get all users for filter dropdown
        List<UserProfile> users = userProfileService.findAll();
        
        model.addAttribute("historyLogs", historyLogs);
        model.addAttribute("users", users);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("selectedUserId", userId);
        model.addAttribute("pageTitle", "History Logs");
        
        return "history-log/list";
    }

    /**
     * Show history log details.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String showHistoryLog(@PathVariable UUID id, Model model) {
        HistoryLog historyLog = historyLogService.findById(id)
                .orElseThrow(() -> new RuntimeException("History log not found with id: " + id));
        
        model.addAttribute("historyLog", historyLog);
        model.addAttribute("pageTitle", "History Log Details");
        
        return "history-log/details";
    }
}