package com.djbc.dutyfree.service;

import com.djbc.dutyfree.domain.dto.request.PassengerCountRequest;
import com.djbc.dutyfree.domain.dto.response.PassengerCountResponse;
import com.djbc.dutyfree.domain.entity.PassengerCount;
import com.djbc.dutyfree.exception.ResourceNotFoundException;
import com.djbc.dutyfree.repository.PassengerCountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PassengerCountService {

    private final PassengerCountRepository passengerCountRepository;

    /**
     * Create or update passenger count for a date
     */
    public PassengerCountResponse createOrUpdatePassengerCount(PassengerCountRequest request) {
        log.info("Creating/updating passenger count for date: {}", request.getCountDate());

        PassengerCount passengerCount = passengerCountRepository.findByCountDate(request.getCountDate())
                .orElse(PassengerCount.builder()
                        .countDate(request.getCountDate())
                        .createdBy(getCurrentUsername())
                        .build());

        updateEntityFromRequest(passengerCount, request);

        // Auto-calculate total if not provided
        if (request.getTotalPassengers() == null &&
            (request.getArrivingPassengers() != null || request.getDepartingPassengers() != null)) {
            passengerCount.calculateTotal();
        }

        passengerCount.setUpdatedBy(getCurrentUsername());
        PassengerCount saved = passengerCountRepository.save(passengerCount);

        log.info("Passenger count saved: {} passengers on {}", saved.getTotalPassengers(), saved.getCountDate());
        return mapToResponse(saved);
    }

    /**
     * Get passenger count by ID
     */
    @Transactional(readOnly = true)
    public PassengerCountResponse getPassengerCountById(Long id) {
        log.debug("Fetching passenger count by ID: {}", id);

        PassengerCount passengerCount = passengerCountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Passenger count not found with id: " + id));

        return mapToResponse(passengerCount);
    }

    /**
     * Get passenger count by date
     */
    @Transactional(readOnly = true)
    public PassengerCountResponse getPassengerCountByDate(LocalDate date) {
        log.debug("Fetching passenger count by date: {}", date);

        PassengerCount passengerCount = passengerCountRepository.findByCountDate(date)
                .orElseThrow(() -> new ResourceNotFoundException("Passenger count not found for date: " + date));

        return mapToResponse(passengerCount);
    }

    /**
     * Get all passenger counts
     */
    @Transactional(readOnly = true)
    public List<PassengerCountResponse> getAllPassengerCounts() {
        log.debug("Fetching all passenger counts");

        return passengerCountRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get passenger counts between dates
     */
    @Transactional(readOnly = true)
    public List<PassengerCountResponse> getPassengerCountsBetween(LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching passenger counts between {} and {}", startDate, endDate);

        return passengerCountRepository.findByCountDateBetween(startDate, endDate).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get passenger counts by flight number
     */
    @Transactional(readOnly = true)
    public List<PassengerCountResponse> getPassengerCountsByFlight(String flightNumber) {
        log.debug("Fetching passenger counts by flight: {}", flightNumber);

        return passengerCountRepository.findByFlightNumber(flightNumber).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get passenger counts by airline
     */
    @Transactional(readOnly = true)
    public List<PassengerCountResponse> getPassengerCountsByAirline(String airline) {
        log.debug("Fetching passenger counts by airline: {}", airline);

        return passengerCountRepository.findByAirline(airline).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get total passengers between dates
     */
    @Transactional(readOnly = true)
    public Integer getTotalPassengersBetween(LocalDate startDate, LocalDate endDate) {
        log.debug("Getting total passengers between {} and {}", startDate, endDate);

        return passengerCountRepository.getTotalPassengersBetween(startDate, endDate);
    }

    /**
     * Get average daily passengers between dates
     */
    @Transactional(readOnly = true)
    public Double getAverageDailyPassengers(LocalDate startDate, LocalDate endDate) {
        log.debug("Getting average daily passengers between {} and {}", startDate, endDate);

        return passengerCountRepository.getAverageDailyPassengers(startDate, endDate);
    }

    /**
     * Delete passenger count
     */
    public void deletePassengerCount(Long id) {
        log.info("Deleting passenger count: {}", id);

        PassengerCount passengerCount = passengerCountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Passenger count not found with id: " + id));

        passengerCountRepository.delete(passengerCount);

        log.info("Passenger count deleted successfully: {}", id);
    }

    // Helper methods

    private void updateEntityFromRequest(PassengerCount passengerCount, PassengerCountRequest request) {
        passengerCount.setCountDate(request.getCountDate());
        passengerCount.setTotalPassengers(request.getTotalPassengers());
        passengerCount.setArrivingPassengers(request.getArrivingPassengers());
        passengerCount.setDepartingPassengers(request.getDepartingPassengers());
        passengerCount.setInternationalPassengers(request.getInternationalPassengers());
        passengerCount.setFlightNumber(request.getFlightNumber());
        passengerCount.setAirline(request.getAirline());
        passengerCount.setDestination(request.getDestination());
        passengerCount.setCountType(request.getCountType());
        passengerCount.setNotes(request.getNotes());
    }

    private PassengerCountResponse mapToResponse(PassengerCount passengerCount) {
        return PassengerCountResponse.builder()
                .id(passengerCount.getId())
                .countDate(passengerCount.getCountDate())
                .totalPassengers(passengerCount.getTotalPassengers())
                .arrivingPassengers(passengerCount.getArrivingPassengers())
                .departingPassengers(passengerCount.getDepartingPassengers())
                .internationalPassengers(passengerCount.getInternationalPassengers())
                .flightNumber(passengerCount.getFlightNumber())
                .airline(passengerCount.getAirline())
                .destination(passengerCount.getDestination())
                .countType(passengerCount.getCountType())
                .notes(passengerCount.getNotes())
                .createdBy(passengerCount.getCreatedBy())
                .updatedBy(passengerCount.getUpdatedBy())
                .createdAt(passengerCount.getCreatedAt())
                .updatedAt(passengerCount.getUpdatedAt())
                .build();
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "system";
    }
}
