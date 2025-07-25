package com.tinusj.stocklee.controller;

import com.tinusj.stocklee.entity.HistoryLog;
import com.tinusj.stocklee.service.HistoryLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for HistoryLog entity operations.
 */
@RestController
@RequestMapping("/api/history-logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class HistoryLogController {

    private final HistoryLogService historyLogService;

    /**
     * Get all history logs.
     */
    @GetMapping
    public ResponseEntity<List<HistoryLog>> getAllHistoryLogs() {
        List<HistoryLog> historyLogs = historyLogService.findAll();
        return ResponseEntity.ok(historyLogs);
    }

    /**
     * Get history log by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<HistoryLog> getHistoryLogById(@PathVariable UUID id) {
        return historyLogService.findById(id)
                .map(historyLog -> ResponseEntity.ok(historyLog))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new history log.
     */
    @PostMapping
    public ResponseEntity<HistoryLog> createHistoryLog(@RequestBody HistoryLog historyLog) {
        HistoryLog savedHistoryLog = historyLogService.save(historyLog);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedHistoryLog);
    }

    /**
     * Update an existing history log.
     */
    @PutMapping("/{id}")
    public ResponseEntity<HistoryLog> updateHistoryLog(@PathVariable UUID id, @RequestBody HistoryLog historyLog) {
        if (!historyLogService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        historyLog.setId(id);
        HistoryLog updatedHistoryLog = historyLogService.save(historyLog);
        return ResponseEntity.ok(updatedHistoryLog);
    }

    /**
     * Delete history log by ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHistoryLog(@PathVariable UUID id) {
        if (!historyLogService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        historyLogService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get count of all history logs.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> getHistoryLogCount() {
        long count = historyLogService.count();
        return ResponseEntity.ok(count);
    }
}