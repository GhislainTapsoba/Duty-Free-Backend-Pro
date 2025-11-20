package com.djbc.dutyfree.controller;

import com.djbc.dutyfree.domain.dto.request.PassengerCountRequest;
import com.djbc.dutyfree.domain.dto.response.PassengerCountResponse;
import com.djbc.dutyfree.service.PassengerCountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/passenger-counts")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PassengerCountController {

    private final PassengerCountService passengerCountService;

    /**
     * Create or update passenger count
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<PassengerCountResponse> createOrUpdatePassengerCount(
            @Valid @RequestBody PassengerCountRequest request) {
        log.info("POST /api/passenger-counts - Creating/updating passenger count for date: {}", request.getCountDate());
        PassengerCountResponse response = passengerCountService.createOrUpdatePassengerCount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get passenger count by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<PassengerCountResponse> getPassengerCountById(@PathVariable Long id) {
        log.info("GET /api/passenger-counts/{} - Fetching passenger count", id);
        PassengerCountResponse response = passengerCountService.getPassengerCountById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get passenger count by date
     */
    @GetMapping("/date/{date}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<PassengerCountResponse> getPassengerCountByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("GET /api/passenger-counts/date/{} - Fetching passenger count by date", date);
        PassengerCountResponse response = passengerCountService.getPassengerCountByDate(date);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all passenger counts
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<List<PassengerCountResponse>> getAllPassengerCounts() {
        log.info("GET /api/passenger-counts - Fetching all passenger counts");
        List<PassengerCountResponse> responses = passengerCountService.getAllPassengerCounts();
        return ResponseEntity.ok(responses);
    }

    /**
     * Get passenger counts between dates
     */
    @GetMapping("/between")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<List<PassengerCountResponse>> getPassengerCountsBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("GET /api/passenger-counts/between - Fetching passenger counts between {} and {}", startDate, endDate);
        List<PassengerCountResponse> responses = passengerCountService.getPassengerCountsBetween(startDate, endDate);
        return ResponseEntity.ok(responses);
    }

    /**
     * Get passenger counts by flight
     */
    @GetMapping("/flight/{flightNumber}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<List<PassengerCountResponse>> getPassengerCountsByFlight(
            @PathVariable String flightNumber) {
        log.info("GET /api/passenger-counts/flight/{} - Fetching passenger counts by flight", flightNumber);
        List<PassengerCountResponse> responses = passengerCountService.getPassengerCountsByFlight(flightNumber);
        return ResponseEntity.ok(responses);
    }

    /**
     * Get passenger counts by airline
     */
    @GetMapping("/airline/{airline}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<List<PassengerCountResponse>> getPassengerCountsByAirline(
            @PathVariable String airline) {
        log.info("GET /api/passenger-counts/airline/{} - Fetching passenger counts by airline", airline);
        List<PassengerCountResponse> responses = passengerCountService.getPassengerCountsByAirline(airline);
        return ResponseEntity.ok(responses);
    }

    /**
     * Get statistics for a period
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, Object>> getPassengerStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("GET /api/passenger-counts/statistics - Getting passenger statistics between {} and {}", startDate, endDate);

        Integer totalPassengers = passengerCountService.getTotalPassengersBetween(startDate, endDate);
        Double averageDaily = passengerCountService.getAverageDailyPassengers(startDate, endDate);

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("startDate", startDate);
        statistics.put("endDate", endDate);
        statistics.put("totalPassengers", totalPassengers);
        statistics.put("averageDailyPassengers", averageDaily);

        return ResponseEntity.ok(statistics);
    }

    /**
     * Delete passenger count
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePassengerCount(@PathVariable Long id) {
        log.info("DELETE /api/passenger-counts/{} - Deleting passenger count", id);
        passengerCountService.deletePassengerCount(id);
        return ResponseEntity.noContent().build();
    }
}
