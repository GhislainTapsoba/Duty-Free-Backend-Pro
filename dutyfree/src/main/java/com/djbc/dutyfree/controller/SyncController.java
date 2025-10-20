package com.djbc.dutyfree.controller;

import com.djbc.dutyfree.domain.dto.response.ApiResponse;
import com.djbc.dutyfree.service.SyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@RestController
@RequestMapping("/api/sync")
@ConditionalOnProperty(name = "spring.redis.enabled", havingValue = "true", matchIfMissing = false)
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Synchronization", description = "Data synchronization APIs")
public class SyncController {

    private final SyncService syncService;

    @PostMapping("/offline-sales")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISEUR')")
    @Operation(summary = "Synchronize offline sales", description = "Sync sales made while offline")
    public ResponseEntity<ApiResponse<Void>> synchronizeOfflineSales() {
        syncService.synchronizeOfflineSales();
        return ResponseEntity.ok(ApiResponse.success("Synchronization completed", null));
    }

    @GetMapping("/pending-count")
    @Operation(summary = "Get pending sales count", description = "Get count of pending offline sales")
    public ResponseEntity<ApiResponse<Long>> getPendingSalesCount() {
        long count = syncService.getPendingSalesCount();
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @GetMapping("/has-pending")
    @Operation(summary = "Check pending sales", description = "Check if there are pending sales to sync")
    public ResponseEntity<ApiResponse<Boolean>> hasPendingSales() {
        boolean hasPending = syncService.hasPendingSales();
        return ResponseEntity.ok(ApiResponse.success(hasPending));
    }

    @GetMapping("/status")
    @Operation(summary = "Get sync status", description = "Get last synchronization status")
    public ResponseEntity<ApiResponse<Object>> getLastSyncStatus() {
        Object status = syncService.getLastSyncStatus();
        return ResponseEntity.ok(ApiResponse.success(status));
    }
}