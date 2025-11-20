package com.djbc.dutyfree.service;

import com.djbc.dutyfree.domain.entity.Sommier;
import com.djbc.dutyfree.domain.enums.SommierStatus;
import com.djbc.dutyfree.exception.BadRequestException;
import com.djbc.dutyfree.exception.ResourceNotFoundException;
import com.djbc.dutyfree.repository.SommierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SommierService {

    private final SommierRepository sommierRepository;

    @Transactional
    public Sommier createSommier(String sommierNumber, BigDecimal initialValue, String notes) {
        // Vérifier si le numéro existe déjà
        if (sommierRepository.findBySommierNumber(sommierNumber).isPresent()) {
            throw new BadRequestException("Sommier number already exists: " + sommierNumber);
        }

        if (initialValue == null || initialValue.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Initial value must be positive");
        }

        Sommier sommier = Sommier.builder()
                .sommierNumber(sommierNumber)
                .currentValue(initialValue)
                .clearedValue(BigDecimal.ZERO)
                .status(SommierStatus.ACTIVE)
                .notes(notes)
                .build();

        sommier = sommierRepository.save(sommier);
        log.info("Created sommier: {}", sommierNumber);
        
        return sommier;
    }

    @Transactional
    public Sommier updateSommierValue(Long sommierId, BigDecimal clearedAmount) {
        Sommier sommier = getSommierById(sommierId);

        if (clearedAmount == null || clearedAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Cleared amount must be positive");
        }

        if (sommier.getStatus() != SommierStatus.ACTIVE) {
            throw new BadRequestException("Can only update value of active sommier");
        }

        sommier.setClearedValue(sommier.getClearedValue().add(clearedAmount));
        sommier.setCurrentValue(sommier.getCurrentValue().subtract(clearedAmount));

        if (sommier.getCurrentValue().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Cleared amount exceeds current value");
        }

        sommier = sommierRepository.save(sommier);
        
        log.info("Updated sommier {} value. Cleared: {}, Remaining: {}", 
                sommier.getSommierNumber(), clearedAmount, sommier.getCurrentValue());
        
        return sommier;
    }

    @Transactional
    public Sommier closeSommier(Long sommierId) {
        Sommier sommier = getSommierById(sommierId);

        if (sommier.getStatus() != SommierStatus.ACTIVE) {
            throw new BadRequestException("Sommier is not active");
        }

        sommier.setStatus(SommierStatus.CLEARED);

        sommier = sommierRepository.save(sommier);
        log.info("Closed sommier: {}", sommier.getSommierNumber());
        
        return sommier;
    }

    @Transactional(readOnly = true)
    public Sommier getSommierById(Long id) {
        return sommierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sommier", "id", id));
    }

    @Transactional(readOnly = true)
    public Sommier getSommierByNumber(String sommierNumber) {
        return sommierRepository.findBySommierNumber(sommierNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Sommier", "number", sommierNumber));
    }

    @Transactional(readOnly = true)
    public List<Sommier> getAllSommiers() {
        log.info("Fetching all sommiers");
        try {
            List<Sommier> sommiers = sommierRepository.findAll();
            log.info("Found {} sommiers", sommiers != null ? sommiers.size() : 0);
            return sommiers != null ? sommiers : List.of();
        } catch (Exception e) {
            log.error("Error fetching all sommiers", e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public List<Sommier> getActiveSommiers() {
        return sommierRepository.findByStatus(SommierStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public List<Sommier> getSommiersByStatus(SommierStatus status) {
        return sommierRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<Sommier> getSommiersNeedingAlert() {
        // Par exemple, les sommiers actifs avec une valeur élevée qui n'ont pas été mis à jour récemment
        BigDecimal alertThreshold = new BigDecimal("10000.00");
        return sommierRepository.findByStatusAndCurrentValueGreaterThan(SommierStatus.ACTIVE, alertThreshold);
    }

    @Transactional(readOnly = true)
    public Long countActiveSommiers() {
        return sommierRepository.countByStatus(SommierStatus.ACTIVE);
    }
}