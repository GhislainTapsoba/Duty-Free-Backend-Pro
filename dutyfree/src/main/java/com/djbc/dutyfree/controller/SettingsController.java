package com.djbc.dutyfree.controller;

import com.djbc.dutyfree.domain.dto.request.BulkSettingsRequest;
import com.djbc.dutyfree.domain.dto.request.SettingsRequest;
import com.djbc.dutyfree.domain.dto.response.ApiResponse;
import com.djbc.dutyfree.domain.dto.response.SettingsResponse;
import com.djbc.dutyfree.service.SettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
@Slf4j
public class SettingsController {

    private final SettingsService settingsService;

    @GetMapping("/{key}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<SettingsResponse>> getSettingByKey(@PathVariable String key) {
        SettingsResponse response = settingsService.getSettingByKey(key);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/category/{category}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<Map<String, String>>> getSettingsByCategory(@PathVariable String category) {
        Map<String, String> settings = settingsService.getSettingsByCategory(category);
        return ResponseEntity.ok(ApiResponse.success(settings));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<Map<String, String>>> getAllSettings() {
        Map<String, String> settings = settingsService.getAllSettings();
        return ResponseEntity.ok(ApiResponse.success(settings));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SettingsResponse>> createOrUpdateSetting(@Valid @RequestBody SettingsRequest request) {
        SettingsResponse response = settingsService.createOrUpdateSetting(request);
        return ResponseEntity.ok(ApiResponse.success("Setting saved successfully", response));
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, String>>> saveBulkSettings(@Valid @RequestBody BulkSettingsRequest request) {
        Map<String, String> settings = settingsService.saveBulkSettings(request);
        return ResponseEntity.ok(ApiResponse.success("Settings saved successfully", settings));
    }

    @DeleteMapping("/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteSetting(@PathVariable String key) {
        settingsService.deleteSetting(key);
        return ResponseEntity.ok(ApiResponse.success("Setting deleted successfully", null));
    }

    @PostMapping("/initialize")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> initializeDefaultSettings() {
        settingsService.initializeDefaultSettings();
        return ResponseEntity.ok(ApiResponse.success("Default settings initialized", null));
    }

    /**
     * Upload receipt logo
     */
    @PostMapping("/receipt/logo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> uploadReceiptLogo(@RequestParam("file") MultipartFile file) {
        log.info("POST /api/settings/receipt/logo - Uploading receipt logo");

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("File is empty"));
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/png") &&
                                    !contentType.equals("image/jpeg") &&
                                    !contentType.equals("image/jpg"))) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Invalid file type. Only PNG and JPEG images are allowed")
            );
        }

        // Validate file size (max 2MB)
        if (file.getSize() > 2 * 1024 * 1024) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("File size too large. Maximum allowed size is 2MB")
            );
        }

        try {
            String logoPath = settingsService.saveReceiptLogo(file);
            return ResponseEntity.ok(ApiResponse.success("Logo uploaded successfully", logoPath));
        } catch (Exception e) {
            log.error("Error uploading logo", e);
            return ResponseEntity.internalServerError().body(
                ApiResponse.error("Failed to upload logo: " + e.getMessage())
            );
        }
    }

    /**
     * Get receipt logo path
     */
    @GetMapping("/receipt/logo")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<String>> getReceiptLogoPath() {
        log.info("GET /api/settings/receipt/logo - Getting receipt logo path");
        String logoPath = settingsService.getReceiptLogoPath();
        return ResponseEntity.ok(ApiResponse.success(logoPath));
    }
}
