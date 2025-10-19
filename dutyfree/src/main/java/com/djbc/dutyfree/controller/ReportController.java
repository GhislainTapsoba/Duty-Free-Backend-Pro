package com.djbc.dutyfree.controller;

import com.djbc.dutyfree.domain.dto.response.ApiResponse;
import com.djbc.dutyfree.domain.dto.response.ReportResponse;
import com.djbc.dutyfree.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Reports", description = "Reporting and analytics APIs")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISEUR')")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/sales")
    @Operation(summary = "Generate sales report", description = "Generate comprehensive sales report")
    public ResponseEntity<ApiResponse<ReportResponse.SalesReport>> generateSalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        ReportResponse.SalesReport report = reportService.generateSalesReport(startDate, endDate);
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
}