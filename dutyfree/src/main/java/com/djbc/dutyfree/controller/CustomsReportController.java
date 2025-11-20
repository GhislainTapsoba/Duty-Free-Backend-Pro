package com.djbc.dutyfree.controller;

import com.djbc.dutyfree.domain.dto.response.ApiResponse;
import com.djbc.dutyfree.service.CustomsReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/customs-reports")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Customs Reports", description = "Customs compliance reports for duty free operations")
public class CustomsReportController {

    private final CustomsReportService customsReportService;

    @GetMapping("/sommier-apurement")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Generate sommier apurement report",
            description = "Generate customs clearance tracking report for a specified period")
    public ResponseEntity<Resource> generateSommierApurement(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("Generating sommier apurement report from {} to {}", startDate, endDate);

        try {
            String pdfPath = customsReportService.generateSommierApurementReport(startDate, endDate);
            return servePdfFile(pdfPath);

        } catch (Exception e) {
            log.error("Error generating sommier apurement report: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/monthly-registry")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Generate monthly customs registry",
            description = "Generate monthly aggregated customs report")
    public ResponseEntity<Resource> generateMonthlyRegistry(
            @RequestParam int year,
            @RequestParam int month) {

        log.info("Generating monthly customs registry for {}-{}", year, month);

        try {
            YearMonth yearMonth = YearMonth.of(year, month);
            String pdfPath = customsReportService.generateMonthlyCustomsRegistry(yearMonth);
            return servePdfFile(pdfPath);

        } catch (Exception e) {
            log.error("Error generating monthly customs registry: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/daily-summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    @Operation(summary = "Generate daily sales summary",
            description = "Generate daily sales summary for customs")
    public ResponseEntity<Resource> generateDailySummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        log.info("Generating daily summary for {}", date);

        try {
            String pdfPath = customsReportService.generateDailySalesSummary(date);
            return servePdfFile(pdfPath);

        } catch (Exception e) {
            log.error("Error generating daily summary: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/current-month")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Generate current month registry",
            description = "Generate customs registry for the current month")
    public ResponseEntity<Resource> generateCurrentMonthRegistry() {
        YearMonth currentMonth = YearMonth.now();
        log.info("Generating current month registry for {}", currentMonth);

        try {
            String pdfPath = customsReportService.generateMonthlyCustomsRegistry(currentMonth);
            return servePdfFile(pdfPath);

        } catch (Exception e) {
            log.error("Error generating current month registry: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/today-summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    @Operation(summary = "Generate today's summary",
            description = "Generate daily sales summary for today")
    public ResponseEntity<Resource> generateTodaySummary() {
        LocalDate today = LocalDate.now();
        log.info("Generating today's summary for {}", today);

        try {
            String pdfPath = customsReportService.generateDailySalesSummary(today);
            return servePdfFile(pdfPath);

        } catch (Exception e) {
            log.error("Error generating today's summary: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/available-reports")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "List available report types",
            description = "Get list of available customs report types and their descriptions")
    public ResponseEntity<ApiResponse<Map<String, String>>> getAvailableReports() {
        Map<String, String> reports = new HashMap<>();
        reports.put("sommier-apurement", "Rapport d'apurement du sommier (tracking des ventes duty-free)");
        reports.put("monthly-registry", "Registre douanier mensuel (récapitulatif mensuel agrégé)");
        reports.put("daily-summary", "Récapitulatif journalier des ventes");

        return ResponseEntity.ok(ApiResponse.success("Available customs reports", reports));
    }

    /**
     * Helper method to serve PDF files
     */
    private ResponseEntity<Resource> servePdfFile(String pdfPath) {
        try {
            File pdfFile = new File(pdfPath);

            if (!pdfFile.exists()) {
                log.error("PDF file not found at path: {}", pdfPath);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            InputStreamResource resource = new InputStreamResource(new FileInputStream(pdfFile));

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=" + pdfFile.getName());
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentLength(pdfFile.length());

            log.info("PDF served successfully: {}", pdfFile.getName());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);

        } catch (Exception e) {
            log.error("Error serving PDF file {}: {}", pdfPath, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
