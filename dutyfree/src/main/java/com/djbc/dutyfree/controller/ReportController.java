package com.djbc.dutyfree.controller;

import com.djbc.dutyfree.domain.dto.response.ApiResponse;
import com.djbc.dutyfree.domain.dto.response.ReportResponse;
import com.djbc.dutyfree.service.ReportService;
import com.djbc.dutyfree.service.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Reports", description = "Reporting and analytics APIs")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISEUR')")
public class ReportController {

    private final ReportService reportService;
    private final ExportService exportService;

    @GetMapping("/sales")
    @Operation(summary = "Generate sales report", description = "Generate comprehensive sales report")
    public ResponseEntity<ApiResponse<ReportResponse.SalesReport>> generateSalesReport(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        LocalDate start, end;
        
        if (period != null) {
            switch (period.toLowerCase()) {
                case "today":
                    start = end = LocalDate.now();
                    break;
                case "yesterday":
                    start = end = LocalDate.now().minusDays(1);
                    break;
                case "week":
                    start = LocalDate.now().minusDays(6);
                    end = LocalDate.now();
                    break;
                case "month":
                    start = LocalDate.now().withDayOfMonth(1);
                    end = LocalDate.now();
                    break;
                default:
                    start = end = LocalDate.now();
            }
        } else if (startDate != null && endDate != null) {
            start = startDate;
            end = endDate;
        } else {
            start = end = LocalDate.now();
        }
        
        ReportResponse.SalesReport report = reportService.generateSalesReport(start, end);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping("/sales/daily")
    @Operation(summary = "Generate daily sales report", description = "Generate sales report for a specific day")
    public ResponseEntity<ApiResponse<Map<String, Object>>> generateDailySalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Map<String, Object> report = reportService.generateDailySalesReport(date);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping("/cashier/{cashierId}")
    @Operation(summary = "Generate cashier report", description = "Generate performance report for a cashier")
    public ResponseEntity<ApiResponse<Map<String, Object>>> generateCashierReport(
            @PathVariable Long cashierId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<String, Object> report = reportService.generateCashierReport(cashierId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping("/cash-register/{cashRegisterId}")
    @Operation(summary = "Generate cash register report", description = "Generate report for a cash register")
    public ResponseEntity<ApiResponse<Map<String, Object>>> generateCashRegisterReport(
            @PathVariable Long cashRegisterId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<String, Object> report = reportService.generateCashRegisterReport(cashRegisterId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping("/test")
    @Operation(summary = "Test endpoint", description = "Test endpoint to verify API connectivity")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testEndpoint() {
        Map<String, Object> testData = new HashMap<>();
        testData.put("status", "OK");
        testData.put("timestamp", LocalDate.now());
        testData.put("message", "Reports API is working");
        return ResponseEntity.ok(ApiResponse.success(testData));
    }

    @GetMapping("/export")
    @Operation(summary = "Export report", description = "Export report in PDF or Excel format")
    public ResponseEntity<byte[]> exportReport(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam String format) {
        
        LocalDate start, end;
        
        if (period != null) {
            switch (period.toLowerCase()) {
                case "today":
                    start = end = LocalDate.now();
                    break;
                case "week":
                    start = LocalDate.now().minusDays(6);
                    end = LocalDate.now();
                    break;
                case "month":
                    start = LocalDate.now().withDayOfMonth(1);
                    end = LocalDate.now();
                    break;
                default:
                    start = end = LocalDate.now();
            }
        } else if (startDate != null && endDate != null) {
            start = startDate;
            end = endDate;
        } else {
            start = end = LocalDate.now();
        }
        
        byte[] data;
        String filename;
        String contentType;
        
        if ("pdf".equals(format)) {
            data = exportService.exportToPdf(start, end);
            filename = "rapport-ventes-" + start + ".pdf";
            contentType = "application/pdf";
        } else {
            data = exportService.exportToExcel(start, end);
            filename = "rapport-ventes-" + start + ".csv";
            contentType = "text/csv";
        }
        
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=" + filename)
                .header("Content-Type", contentType)
                .body(data);
    }

    @GetMapping("/capture-rate")
    @Operation(summary = "Generate capture rate report", description = "Generate capture rate report (tickets / passengers)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> generateCaptureRateReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<String, Object> report = reportService.generateCaptureRateReport(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping("/sales-by-pos")
    @Operation(summary = "Generate sales by POS report", description = "Generate sales by point of sale (cash register) report")
    public ResponseEntity<ApiResponse<java.util.List<Map<String, Object>>>> generateSalesByPOSReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        java.util.List<Map<String, Object>> report = reportService.generateSalesByPOSReport(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping("/average-ticket")
    @Operation(summary = "Generate average ticket report", description = "Generate detailed average ticket report")
    public ResponseEntity<ApiResponse<Map<String, Object>>> generateAverageTicketReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<String, Object> report = reportService.generateAverageTicketReport(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(report));
    }
}