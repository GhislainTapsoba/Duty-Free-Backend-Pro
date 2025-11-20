package com.djbc.dutyfree.service;

import com.djbc.dutyfree.domain.dto.request.BulkSettingsRequest;
import com.djbc.dutyfree.domain.dto.request.SettingsRequest;
import com.djbc.dutyfree.domain.dto.response.SettingsResponse;
import com.djbc.dutyfree.domain.entity.Settings;
import com.djbc.dutyfree.repository.SettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettingsService {

    private final SettingsRepository settingsRepository;

    @Transactional(readOnly = true)
    public SettingsResponse getSettingByKey(String key) {
        Settings settings = settingsRepository.findByKey(key)
                .orElseThrow(() -> new RuntimeException("Setting not found: " + key));
        return mapToResponse(settings);
    }

    @Transactional(readOnly = true)
    public Map<String, String> getSettingsByCategory(String category) {
        List<Settings> settingsList = settingsRepository.findByCategory(category);
        Map<String, String> settingsMap = new HashMap<>();
        for (Settings setting : settingsList) {
            settingsMap.put(setting.getKey(), setting.getValue());
        }
        return settingsMap;
    }

    @Transactional(readOnly = true)
    public Map<String, String> getAllSettings() {
        List<Settings> settingsList = settingsRepository.findAll();
        Map<String, String> settingsMap = new HashMap<>();
        for (Settings setting : settingsList) {
            settingsMap.put(setting.getKey(), setting.getValue());
        }
        return settingsMap;
    }

    @Transactional
    public SettingsResponse createOrUpdateSetting(SettingsRequest request) {
        Settings settings = settingsRepository.findByKey(request.getKey())
                .orElse(Settings.builder()
                        .key(request.getKey())
                        .category(request.getCategory())
                        .description(request.getDescription())
                        .build());

        settings.setValue(request.getValue());
        if (request.getCategory() != null) {
            settings.setCategory(request.getCategory());
        }
        if (request.getDescription() != null) {
            settings.setDescription(request.getDescription());
        }

        Settings savedSettings = settingsRepository.save(settings);
        log.info("Setting saved: {} = {}", savedSettings.getKey(), savedSettings.getValue());
        return mapToResponse(savedSettings);
    }

    @Transactional
    public Map<String, String> saveBulkSettings(BulkSettingsRequest request) {
        Map<String, String> savedSettings = new HashMap<>();

        for (Map.Entry<String, String> entry : request.getSettings().entrySet()) {
            SettingsRequest settingRequest = SettingsRequest.builder()
                    .key(entry.getKey())
                    .value(entry.getValue())
                    .category(request.getCategory())
                    .build();

            SettingsResponse response = createOrUpdateSetting(settingRequest);
            savedSettings.put(response.getKey(), response.getValue());
        }

        log.info("Bulk settings saved: {} settings in category {}", savedSettings.size(), request.getCategory());
        return savedSettings;
    }

    @Transactional
    public void deleteSetting(String key) {
        Settings settings = settingsRepository.findByKey(key)
                .orElseThrow(() -> new RuntimeException("Setting not found: " + key));
        settingsRepository.delete(settings);
        log.info("Setting deleted: {}", key);
    }

    @Transactional
    public void initializeDefaultSettings() {
        // Initialize default settings if they don't exist
        createDefaultSettingIfNotExists("currency", "XOF", "general", "Default currency");
        createDefaultSettingIfNotExists("taxRate", "18", "general", "Default tax rate percentage");
        createDefaultSettingIfNotExists("language", "Français", "general", "Default language");
        createDefaultSettingIfNotExists("companyName", "DJBC Duty Free", "company", "Company name");
        createDefaultSettingIfNotExists("address", "Aéroport International de Ouagadougou", "company", "Company address");
        createDefaultSettingIfNotExists("phone", "+226 XX XX XX XX", "company", "Company phone");
        createDefaultSettingIfNotExists("taxId", "", "company", "Tax identification number");
    }

    private void createDefaultSettingIfNotExists(String key, String value, String category, String description) {
        if (!settingsRepository.existsByKey(key)) {
            Settings setting = Settings.builder()
                    .key(key)
                    .value(value)
                    .category(category)
                    .description(description)
                    .build();
            settingsRepository.save(setting);
            log.info("Default setting created: {} = {}", key, value);
        }
    }

    /**
     * Save receipt logo file and update settings
     */
    @Transactional
    public String saveReceiptLogo(MultipartFile file) throws IOException {
        log.info("Saving receipt logo: {}", file.getOriginalFilename());

        // Create uploads directory if it doesn't exist
        String uploadsDir = "uploads/logos";
        Path uploadPath = Paths.get(uploadsDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            log.info("Created uploads directory: {}", uploadPath.toAbsolutePath());
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".png";
        String filename = "logo_" + UUID.randomUUID() + extension;

        // Save file
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        String absolutePath = filePath.toAbsolutePath().toString();
        log.info("Logo saved to: {}", absolutePath);

        // Update settings with logo path
        SettingsRequest settingRequest = SettingsRequest.builder()
                .key("receipt.logo.path")
                .value(absolutePath)
                .category("receipt")
                .description("Path to receipt logo image")
                .build();

        createOrUpdateSetting(settingRequest);

        return absolutePath;
    }

    /**
     * Get receipt logo path from settings
     */
    @Transactional(readOnly = true)
    public String getReceiptLogoPath() {
        return settingsRepository.findByKey("receipt.logo.path")
                .map(Settings::getValue)
                .orElse(null);
    }

    private SettingsResponse mapToResponse(Settings settings) {
        return SettingsResponse.builder()
                .id(settings.getId())
                .key(settings.getKey())
                .value(settings.getValue())
                .category(settings.getCategory())
                .description(settings.getDescription())
                .createdAt(settings.getCreatedAt())
                .updatedAt(settings.getUpdatedAt())
                .build();
    }
}
