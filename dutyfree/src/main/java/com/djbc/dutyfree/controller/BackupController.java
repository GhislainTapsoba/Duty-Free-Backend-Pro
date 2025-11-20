package com.djbc.dutyfree.controller;

import com.djbc.dutyfree.domain.dto.response.ApiResponse;
import com.djbc.dutyfree.service.BackupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/backups")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Backups", description = "Database and file backup management")
public class BackupController {

    private final BackupService backupService;

    @PostMapping("/full")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create full backup", description = "Manually trigger a full backup (database + files)")
    public ResponseEntity<ApiResponse<Map<String, String>>> createFullBackup() {
        log.info("Manual full backup requested");

        try {
            String timestamp = backupService.performFullBackup();

            Map<String, String> result = new HashMap<>();
            result.put("timestamp", timestamp);
            result.put("status", "completed");
            result.put("message", "Full backup completed successfully");

            return ResponseEntity.ok(ApiResponse.success("Backup created successfully", result));

        } catch (Exception e) {
            log.error("Full backup failed: {}", e.getMessage(), e);

            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("status", "failed");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Backup failed: " + e.getMessage()));
        }
    }

    @PostMapping("/database")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create database backup", description = "Manually trigger a database-only backup")
    public ResponseEntity<ApiResponse<Map<String, String>>> createDatabaseBackup() {
        log.info("Manual database backup requested");

        try {
            String timestamp = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

            String backupPath = backupService.backupDatabase(timestamp);

            Map<String, String> result = new HashMap<>();
            result.put("timestamp", timestamp);
            result.put("path", backupPath);
            result.put("status", "completed");

            return ResponseEntity.ok(ApiResponse.success("Database backup created successfully", result));

        } catch (Exception e) {
            log.error("Database backup failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Database backup failed: " + e.getMessage()));
        }
    }

    @PostMapping("/files")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create files backup", description = "Manually trigger a files-only backup")
    public ResponseEntity<ApiResponse<Map<String, String>>> createFilesBackup() {
        log.info("Manual files backup requested");

        try {
            String timestamp = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

            String backupPath = backupService.backupFiles(timestamp);

            Map<String, String> result = new HashMap<>();
            result.put("timestamp", timestamp);
            result.put("path", backupPath);
            result.put("status", "completed");

            return ResponseEntity.ok(ApiResponse.success("Files backup created successfully", result));

        } catch (Exception e) {
            log.error("Files backup failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Files backup failed: " + e.getMessage()));
        }
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all backups", description = "Get list of all available backups")
    public ResponseEntity<ApiResponse<List<BackupService.BackupInfo>>> listBackups() {
        log.info("Listing all backups");

        try {
            List<BackupService.BackupInfo> backups = backupService.listBackups();
            return ResponseEntity.ok(ApiResponse.success("Backups retrieved successfully", backups));

        } catch (Exception e) {
            log.error("Failed to list backups: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to list backups: " + e.getMessage()));
        }
    }

    @PostMapping("/restore/database")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Restore database", description = "Restore database from a backup file")
    public ResponseEntity<ApiResponse<Map<String, String>>> restoreDatabase(
            @RequestParam String backupFileName) {

        log.info("Database restoration requested from: {}", backupFileName);

        try {
            backupService.restoreDatabase(backupFileName);

            Map<String, String> result = new HashMap<>();
            result.put("backupFile", backupFileName);
            result.put("status", "completed");
            result.put("message", "Database restored successfully");

            return ResponseEntity.ok(ApiResponse.success("Database restored successfully", result));

        } catch (Exception e) {
            log.error("Database restoration failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Database restoration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/restore/files")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Restore files", description = "Restore files from a backup")
    public ResponseEntity<ApiResponse<Map<String, String>>> restoreFiles(
            @RequestParam String backupFileName) {

        log.info("Files restoration requested from: {}", backupFileName);

        try {
            backupService.restoreFiles(backupFileName);

            Map<String, String> result = new HashMap<>();
            result.put("backupFile", backupFileName);
            result.put("status", "completed");
            result.put("message", "Files restored successfully");

            return ResponseEntity.ok(ApiResponse.success("Files restored successfully", result));

        } catch (Exception e) {
            log.error("Files restoration failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Files restoration failed: " + e.getMessage()));
        }
    }

    @DeleteMapping("/cleanup")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cleanup old backups", description = "Delete backups older than retention period")
    public ResponseEntity<ApiResponse<Map<String, Object>>> cleanupOldBackups() {
        log.info("Backup cleanup requested");

        try {
            int deletedCount = backupService.cleanupOldBackups();

            Map<String, Object> result = new HashMap<>();
            result.put("deletedCount", deletedCount);
            result.put("status", "completed");
            result.put("message", String.format("%d old backups deleted", deletedCount));

            return ResponseEntity.ok(ApiResponse.success("Cleanup completed successfully", result));

        } catch (Exception e) {
            log.error("Backup cleanup failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Cleanup failed: " + e.getMessage()));
        }
    }

    @GetMapping("/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get backup status", description = "Get backup system status and statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBackupStatus() {
        log.info("Backup status requested");

        try {
            List<BackupService.BackupInfo> backups = backupService.listBackups();

            Map<String, Object> status = new HashMap<>();
            status.put("totalBackups", backups.size());
            status.put("latestBackup", backups.isEmpty() ? null : backups.get(0));
            status.put("databaseBackups", backups.stream().filter(b -> "database".equals(b.getType())).count());
            status.put("filesBackups", backups.stream().filter(b -> "files".equals(b.getType())).count());

            long totalSize = backups.stream().mapToLong(BackupService.BackupInfo::getSize).sum();
            status.put("totalSize", totalSize);
            status.put("totalSizeMB", totalSize / (1024.0 * 1024.0));

            return ResponseEntity.ok(ApiResponse.success("Backup status retrieved successfully", status));

        } catch (Exception e) {
            log.error("Failed to get backup status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get backup status: " + e.getMessage()));
        }
    }
}
