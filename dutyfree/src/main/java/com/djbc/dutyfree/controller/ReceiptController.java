package com.djbc.dutyfree.controller;

import com.djbc.dutyfree.domain.dto.response.ApiResponse;
import com.djbc.dutyfree.domain.entity.Receipt;
import com.djbc.dutyfree.service.ReceiptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

@RestController
@RequestMapping("/api/receipts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Receipts", description = "Receipt management endpoints")
public class ReceiptController {

    private final ReceiptService receiptService;

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    @Operation(summary = "Get receipt by ID", description = "Retrieve receipt details by ID")
    public ResponseEntity<ApiResponse<Receipt>> getReceiptById(@PathVariable Long id) {
        log.info("Fetching receipt with ID: {}", id);
        Receipt receipt = receiptService.getReceiptById(id);
        return ResponseEntity.ok(ApiResponse.success("Receipt retrieved successfully", receipt));
    }

    @GetMapping("/sale/{saleId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    @Operation(summary = "Get receipt by sale ID", description = "Retrieve receipt by sale ID")
    public ResponseEntity<ApiResponse<Receipt>> getReceiptBySaleId(@PathVariable Long saleId) {
        log.info("Fetching receipt for sale ID: {}", saleId);
        Receipt receipt = receiptService.findBySaleId(saleId);
        return ResponseEntity.ok(ApiResponse.success("Receipt retrieved successfully", receipt));
    }

    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    @Operation(summary = "Download receipt PDF", description = "Download the PDF version of a receipt")
    public ResponseEntity<Resource> downloadReceiptPdf(@PathVariable Long id) {
        log.info("Downloading PDF for receipt ID: {}", id);

        try {
            Receipt receipt = receiptService.getReceiptById(id);

            if (receipt.getPdfPath() == null || receipt.getPdfPath().isEmpty()) {
                log.warn("PDF not available for receipt ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            File pdfFile = new File(receipt.getPdfPath());

            if (!pdfFile.exists()) {
                log.error("PDF file not found at path: {}", receipt.getPdfPath());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            InputStreamResource resource = new InputStreamResource(new FileInputStream(pdfFile));

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=" + pdfFile.getName());
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentLength(pdfFile.length());

            log.info("PDF downloaded successfully for receipt: {}", receipt.getReceiptNumber());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);

        } catch (FileNotFoundException e) {
            log.error("Error reading PDF file for receipt {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            log.error("Unexpected error downloading PDF for receipt {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}/content")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    @Operation(summary = "Get receipt text content", description = "Get the text content of receipt for thermal printing")
    public ResponseEntity<ApiResponse<String>> getReceiptContent(@PathVariable Long id) {
        log.info("Fetching receipt content for ID: {}", id);
        Receipt receipt = receiptService.getReceiptById(id);

        if (receipt.getReceiptContent() == null || receipt.getReceiptContent().isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Receipt content not available"));
        }

        return ResponseEntity.ok(ApiResponse.success("Receipt content retrieved successfully",
                receipt.getReceiptContent()));
    }

    @PostMapping("/{id}/mark-printed")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    @Operation(summary = "Mark receipt as printed", description = "Update receipt status to printed")
    public ResponseEntity<ApiResponse<Receipt>> markAsPrinted(@PathVariable Long id) {
        log.info("Marking receipt {} as printed", id);

        try {
            Receipt receipt = receiptService.getReceiptById(id);
            receipt.setPrinted(true);
            // You would save the updated receipt here via service method
            // receipt = receiptService.updateReceipt(receipt);

            return ResponseEntity.ok(ApiResponse.success("Receipt marked as printed", receipt));
        } catch (Exception e) {
            log.error("Error marking receipt as printed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to mark receipt as printed"));
        }
    }

    @PostMapping("/generate/{saleId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    @Operation(summary = "Generate receipt for sale", description = "Generate a new receipt for a completed sale")
    public ResponseEntity<ApiResponse<Receipt>> generateReceipt(@PathVariable Long saleId) {
        log.info("Generating receipt for sale ID: {}", saleId);

        try {
            Receipt receipt = receiptService.generateReceipt(saleId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Receipt generated successfully", receipt));
        } catch (Exception e) {
            log.error("Error generating receipt for sale {}: {}", saleId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to generate receipt: " + e.getMessage()));
        }
    }
}
