package com.djbc.dutyfree.service;

import com.djbc.dutyfree.domain.entity.Sommier;
import com.djbc.dutyfree.domain.enums.SommierStatus;
import com.djbc.dutyfree.exception.BadRequestException;
import com.djbc.dutyfree.exception.ResourceNotFoundException;
import com.djbc.dutyfree.repository.SommierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SommierService {

    private final SommierRepository sommierRepository;

    @Transactional
    public Sommier createSommier(String sommierNumber, BigDecimal initialValue, String notes) {
        if (sommierRepository.existsBySommierNumber(sommierNumber)) {
            throw new BadRequestException("Sommier with number " + sommierNumber + " already exists");
        }

        Sommier sommier = Sommier.builder()
                .sommierNumber(sommierNumber)
                .openingDate(LocalDate.now())
                .initialValue(initialValue)
                .currentValue(initialValue)
                .clearedValue(BigDecimal.ZERO)
                .status(SommierStatus.ACTIVE)
                .notes(notes)
                .alertSent(false)
                .build();

        // Set alert date (90 days from opening)
        sommier.setAlertDate(LocalDate.now().plusDays(90));

        sommier = sommierRepository.save(sommier);
        log.info("Sommier created: {}", sommierNumber);

        return sommier;
    }

    @Transactional
    public Sommier updateSommierValue(Long sommierId, BigDecimal clearedAmount) {
        Sommier sommier = sommierRepository.findById(sommierId)
                .orElseThrow(() -> new ResourceNotFoundException("Sommier", "id", sommierId));

        if (sommier.getStatus() == SommierStatus.CLEARED) {
            throw new BadRequestException("Cannot update cleared sommier");
        }

        BigDecimal newClearedValue = sommier.getClearedValue().add(clearedAmount);

        if (newClearedValue.compareTo(sommier.getInitialValue()) > 0) {
            throw new BadRequestException("Cleared value cannot exceed initial value");
        }

        sommier.setClearedValue(newClearedValue);
        sommier.setCurrentValue(sommier.getInitialValue().subtract(newClearedValue));

        // Update status based on cleared value
        if (newClearedValue.compareTo(sommier.getInitialValue()) == 0) {
            sommier.setStatus(SommierStatus.CLEARED);
            sommier.setClosingDate(LocalDate.now());
            log.info("Sommier fully cleared: {}", sommier.getSommierNumber());
        } else if (newClearedValue.compareTo(BigDecimal.ZERO) > 0) {
            sommier.setStatus(SommierStatus.PARTIALLY_CLEARED);
        }

        sommier = sommierRepository.save(sommier);
        return sommier;
    }

    @Transactional
    public Sommier closeSommier(Long sommierId) {
        Sommier sommier = sommierRepository.findById(sommierId)
                .orElseThrow(() -> new ResourceNotFoundException("Sommier", "id", sommierId));

        if (sommier.getStatus() == SommierStatus.CLEARED) {
            throw new BadRequestException("Sommier already cleared");
        }

        sommier.setStatus(SommierStatus.CLEARED);
        sommier.setClosingDate(LocalDate.now());
        sommier = sommierRepository.save(sommier);

        log.info("Sommier closed: {}", sommier.getSommierNumber());
        return sommier;
    }

    @Transactional(readOnly = true)
    public Sommier getSommierById(Long id) {
        return sommierRepository.findByIdWithStocks(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sommier", "id", id));
    }

    @Transactional(readOnly = true)
    public Sommier getSommierByNumber(String sommierNumber) {
        return sommierRepository.findBySommierNumber(sommierNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Sommier", "sommierNumber", sommierNumber));
    }

    @Transactional(readOnly = true)
    public List<Sommier> getActiveSommiers() {
        return sommierRepository.findActiveSommiersByStatus(SommierStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public List<Sommier> getSommiersByStatus(SommierStatus status) {
        return sommierRepository.findActiveSommiersByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<Sommier> getSommiersNeedingAlert() {
        return sommierRepository.findSommiersNeedingAlert(LocalDate.now());
    }

    @Transactional
    @Scheduled(cron = "0 0 9 * * *") // Every day at 9 AM
    public void checkSommierAlerts() {
        List<Sommier> sommiers = getSommiersNeedingAlert();

        for (Sommier sommier : sommiers) {
            // TODO: Send alert notification (email, SMS, etc.)
            log.warn("Alert: Sommier {} needs clearing. Days since opening: {}",
                    sommier.getSommierNumber(),
                    LocalDate.now().toEpochDay() - sommier.getOpeningDate().toEpochDay());

            sommier.setAlertSent(true);
            sommierRepository.save(sommier);
        }
    }

    @Transactional(readOnly = true)
    public Long countActiveSommiers() {
        return sommierRepository.countActiveSommiers();
    }
}