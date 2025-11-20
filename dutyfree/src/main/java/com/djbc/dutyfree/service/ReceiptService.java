package com.djbc.dutyfree.service;

import com.djbc.dutyfree.domain.entity.Receipt;
import com.djbc.dutyfree.domain.entity.Sale;
import com.djbc.dutyfree.domain.entity.Settings;
import com.djbc.dutyfree.repository.ReceiptRepository;
import com.djbc.dutyfree.repository.SaleRepository;
import com.djbc.dutyfree.repository.SettingsRepository;
import com.djbc.dutyfree.util.ReceiptGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReceiptService {

    private final ReceiptRepository receiptRepository;
    private final SaleRepository saleRepository;
    private final SettingsRepository settingsRepository;
    private final ReceiptGenerator receiptGenerator;

    @Value("${app.receipts.storage-path:./receipts}")
    private String receiptsStoragePath;

    @Transactional
    public Receipt generateReceipt(Long saleId) {
        log.info("Generating receipt for sale ID: {}", saleId);

        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new RuntimeException("Sale not found with ID: " + saleId));

        // Create receipt number
        String receiptNumber = generateReceiptNumber();

        // Create receipt entity
        Receipt receipt = Receipt.builder()
                .receiptNumber(receiptNumber)
                .sale(sale)
                .printedDate(LocalDateTime.now())
                .printed(false)
                .emailed(false)
                .build();

        // Save receipt first to get ID
        receipt = receiptRepository.save(receipt);

        try {
            // Fetch company settings
            Map<String, String> companySettings = fetchCompanySettings();

            // Generate text content for receipt (for thermal printer)
            String receiptText = receiptGenerator.generateReceiptText(sale, receiptNumber);
            receipt.setReceiptContent(receiptText);

            // Generate PDF
            String pdfFileName = generatePdfFileName(receipt);
            String pdfPath = Paths.get(receiptsStoragePath, pdfFileName).toString();

            String generatedPath = receiptGenerator.generatePDF(receipt, companySettings, pdfPath);
            receipt.setPdfPath(generatedPath);

            log.info("Receipt generated successfully: {} with PDF at: {}", receiptNumber, generatedPath);

        } catch (Exception e) {
            log.error("Error generating PDF receipt for sale {}: {}", saleId, e.getMessage(), e);
            // Continue without PDF - text receipt is still available
            receipt.setReceiptContent(receiptGenerator.generateReceiptText(sale, receiptNumber));
        }

        return receiptRepository.save(receipt);
    }

    private String generateReceiptNumber() {
        LocalDateTime now = LocalDateTime.now();
        String prefix = String.format("REC-%d%02d%02d-", now.getYear(), now.getMonthValue(), now.getDayOfMonth());
        Long count = receiptRepository.count() + 1;
        return prefix + String.format("%06d", count);
    }

    private String generatePdfFileName(Receipt receipt) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return String.format("receipt_%s_%s.pdf",
                receipt.getReceiptNumber().replace("/", "_"),
                now.format(formatter));
    }

    private Map<String, String> fetchCompanySettings() {
        Map<String, String> settings = new HashMap<>();

        // Fetch all company and general settings
        List<Settings> companySettings = settingsRepository.findByCategory("company");
        List<Settings> generalSettings = settingsRepository.findByCategory("general");

        // Add company settings
        for (Settings setting : companySettings) {
            settings.put(setting.getKey(), setting.getValue());
        }

        // Add general settings
        for (Settings setting : generalSettings) {
            settings.put(setting.getKey(), setting.getValue());
        }

        // Set defaults if not found
        settings.putIfAbsent("companyName", "DJBC DUTY FREE");
        settings.putIfAbsent("companyAddress", "Aéroport International de Ouagadougou");
        settings.putIfAbsent("companyPhone", "+226 XX XX XX XX");
        settings.putIfAbsent("companyEmail", "contact@djbc-dutyfree.bf");
        settings.putIfAbsent("ifu", "00000000000000");
        settings.putIfAbsent("rccm", "BF-OUA-XXXXXX");
        settings.putIfAbsent("customsNumber", "XXXX/DGD/XX");

        return settings;
    }

    /**
     * Get receipt by ID
     */
    public Receipt getReceiptById(Long id) {
        return receiptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Receipt not found with ID: " + id));
    }

    /**
     * Find receipt by sale ID
     */
    public Receipt findBySaleId(Long saleId) {
        return receiptRepository.findAll().stream()
                .filter(r -> r.getSale().getId().equals(saleId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Receipt not found for sale ID: " + saleId));
    }
}