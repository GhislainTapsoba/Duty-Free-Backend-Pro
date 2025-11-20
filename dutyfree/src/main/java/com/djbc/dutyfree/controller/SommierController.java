package com.djbc.dutyfree.controller;

import com.djbc.dutyfree.domain.dto.response.ApiResponse;
import com.djbc.dutyfree.domain.dto.response.SommierResponse;
import com.djbc.dutyfree.domain.entity.Sommier;
import com.djbc.dutyfree.domain.enums.SommierStatus;
import com.djbc.dutyfree.domain.mapper.SommierMapper;
import com.djbc.dutyfree.service.SommierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/sommiers")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Sommiers", description = "Sommier management APIs")
public class SommierController {

    private final SommierService sommierService;

    @PostMapping
    @Operation(summary = "Create sommier", description = "Create a new sommier")
    public ResponseEntity<ApiResponse<SommierResponse>> createSommier(
            @RequestParam String sommierNumber,
            @RequestParam BigDecimal initialValue,
            @RequestParam(required = false) String notes) {
        Sommier sommier = sommierService.createSommier(sommierNumber, initialValue, notes);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Sommier created successfully", SommierMapper.toResponse(sommier)));
    }

    @PutMapping("/{sommierId}/update-value")
    @Operation(summary = "Update sommier value", description = "Update cleared value of sommier")
    public ResponseEntity<ApiResponse<SommierResponse>> updateSommierValue(
            @PathVariable Long sommierId,
            @RequestParam BigDecimal clearedAmount) {
        Sommier sommier = sommierService.updateSommierValue(sommierId, clearedAmount);
        return ResponseEntity.ok(ApiResponse.success("Sommier value updated successfully", SommierMapper.toResponse(sommier)));
    }

    @PostMapping("/{sommierId}/close")
    @Operation(summary = "Close sommier", description = "Close a sommier")
    public ResponseEntity<ApiResponse<SommierResponse>> closeSommier(@PathVariable Long sommierId) {
        Sommier sommier = sommierService.closeSommier(sommierId);
        return ResponseEntity.ok(ApiResponse.success("Sommier closed successfully", SommierMapper.toResponse(sommier)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get sommier by ID", description = "Get sommier details by ID")
    public ResponseEntity<ApiResponse<SommierResponse>> getSommierById(@PathVariable Long id) {
        Sommier sommier = sommierService.getSommierById(id);
        return ResponseEntity.ok(ApiResponse.success(SommierMapper.toResponse(sommier)));
    }

    @GetMapping("/number/{sommierNumber}")
    @Operation(summary = "Get sommier by number", description = "Get sommier by sommier number")
    public ResponseEntity<ApiResponse<SommierResponse>> getSommierByNumber(@PathVariable String sommierNumber) {
        Sommier sommier = sommierService.getSommierByNumber(sommierNumber);
        return ResponseEntity.ok(ApiResponse.success(SommierMapper.toResponse(sommier)));
    }

    @GetMapping
    @Operation(summary = "Get all sommiers", description = "Get all sommiers")
    public ResponseEntity<ApiResponse<List<SommierResponse>>> getAllSommiers() {
        log.info("Fetching all sommiers");
        List<Sommier> sommiers = sommierService.getAllSommiers();
        List<SommierResponse> responses = SommierMapper.toResponseList(sommiers);
        log.info("Successfully retrieved {} sommiers", responses.size());
        return ResponseEntity.ok(ApiResponse.success("Sommiers retrieved successfully", responses));
    }

    @GetMapping("/active")
    @Operation(summary = "Get active sommiers", description = "Get all active sommiers")
    public ResponseEntity<ApiResponse<List<SommierResponse>>> getActiveSommiers() {
        List<Sommier> sommiers = sommierService.getActiveSommiers();
        return ResponseEntity.ok(ApiResponse.success(SommierMapper.toResponseList(sommiers)));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get sommiers by status", description = "Get sommiers by status")
    public ResponseEntity<ApiResponse<List<SommierResponse>>> getSommiersByStatus(@PathVariable SommierStatus status) {
        List<Sommier> sommiers = sommierService.getSommiersByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(SommierMapper.toResponseList(sommiers)));
    }

    @GetMapping("/alerts")
    @Operation(summary = "Get sommiers needing alert", description = "Get sommiers that need clearing alert")
    public ResponseEntity<ApiResponse<List<SommierResponse>>> getSommiersNeedingAlert() {
        List<Sommier> sommiers = sommierService.getSommiersNeedingAlert();
        return ResponseEntity.ok(ApiResponse.success(SommierMapper.toResponseList(sommiers)));
    }

    @GetMapping("/count/active")
    @Operation(summary = "Count active sommiers", description = "Get count of active sommiers")
    public ResponseEntity<ApiResponse<Long>> countActiveSommiers() {
        Long count = sommierService.countActiveSommiers();
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}