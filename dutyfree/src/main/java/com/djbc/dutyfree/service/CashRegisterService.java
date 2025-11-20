package com.djbc.dutyfree.service;

import com.djbc.dutyfree.domain.dto.request.CashRegisterRequest;
import com.djbc.dutyfree.domain.dto.response.CashRegisterResponse;
import com.djbc.dutyfree.domain.entity.CashRegister;
import com.djbc.dutyfree.domain.entity.User;
import com.djbc.dutyfree.exception.BadRequestException;
import com.djbc.dutyfree.exception.ResourceNotFoundException;
import com.djbc.dutyfree.repository.CashRegisterRepository;
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
public class CashRegisterService {

    private final CashRegisterRepository cashRegisterRepository;
    private final AuthService authService;

    @Transactional
    public CashRegister createCashRegister(CashRegisterRequest request) {
        if (cashRegisterRepository.existsByRegisterNumber(request.getRegisterNumber())) {
            throw new BadRequestException("Cash register with number " + request.getRegisterNumber() + " already exists");
        }

        CashRegister cashRegister = CashRegister.builder()
                .registerNumber(request.getRegisterNumber())
                .name(request.getName())
                .location(request.getLocation())
                .active(request.getActive() != null ? request.getActive() : true)
                .isOpen(request.getIsOpen() != null ? request.getIsOpen() : false)
                .openingBalance(request.getOpeningBalance() != null ? request.getOpeningBalance() : BigDecimal.ZERO)
                .closingBalance(request.getClosingBalance() != null ? request.getClosingBalance() : BigDecimal.ZERO)
                .expectedBalance(request.getExpectedBalance() != null ? request.getExpectedBalance() : BigDecimal.ZERO)
                .cashInDrawer(request.getCashInDrawer() != null ? request.getCashInDrawer() : BigDecimal.ZERO)
                .build();

        cashRegister = cashRegisterRepository.save(cashRegister);
        log.info("Cash register created: {}", request.getRegisterNumber());

        return cashRegister;
    }

    @Transactional
    public CashRegister openCashRegister(Long cashRegisterId, BigDecimal openingBalance) {
        CashRegister cashRegister = cashRegisterRepository.findById(cashRegisterId)
                .orElseThrow(() -> new ResourceNotFoundException("CashRegister", "id", cashRegisterId));

        if (cashRegister.getIsOpen()) {
            throw new BadRequestException("Cash register is already open");
        }

        User currentUser = authService.getCurrentUser();

        cashRegister.setIsOpen(true);
        cashRegister.setOpenedAt(LocalDateTime.now());
        cashRegister.setOpenedBy(currentUser);
        cashRegister.setOpeningBalance(openingBalance);
        cashRegister.setCashInDrawer(openingBalance);
        cashRegister.setExpectedBalance(openingBalance);

        cashRegister = cashRegisterRepository.save(cashRegister);
        log.info("Cash register opened: {} by {}", cashRegister.getRegisterNumber(), currentUser.getUsername());

        return cashRegister;
    }

    @Transactional
    public CashRegister closeCashRegister(Long cashRegisterId, BigDecimal closingBalance) {
        CashRegister cashRegister = cashRegisterRepository.findById(cashRegisterId)
                .orElseThrow(() -> new ResourceNotFoundException("CashRegister", "id", cashRegisterId));

        if (!cashRegister.getIsOpen()) {
            throw new BadRequestException("Cash register is not open");
        }

        User currentUser = authService.getCurrentUser();

        cashRegister.setIsOpen(false);
        cashRegister.setClosedAt(LocalDateTime.now());
        cashRegister.setClosedBy(currentUser);
        cashRegister.setClosingBalance(closingBalance);

        // Calculate variance
        BigDecimal variance = closingBalance.subtract(cashRegister.getExpectedBalance());

        cashRegister = cashRegisterRepository.save(cashRegister);
        log.info("Cash register closed: {} by {}. Variance: {}",
                cashRegister.getRegisterNumber(), currentUser.getUsername(), variance);

        return cashRegister;
    }

    @Transactional
    public void addCash(Long cashRegisterId, BigDecimal amount) {
        CashRegister cashRegister = cashRegisterRepository.findById(cashRegisterId)
                .orElseThrow(() -> new ResourceNotFoundException("CashRegister", "id", cashRegisterId));

        cashRegister.setCashInDrawer(cashRegister.getCashInDrawer().add(amount));
        cashRegister.setExpectedBalance(cashRegister.getExpectedBalance().add(amount));
        cashRegisterRepository.save(cashRegister);

        log.info("Added {} to cash register {}", amount, cashRegister.getRegisterNumber());
    }

    @Transactional
    public void removeCash(Long cashRegisterId, BigDecimal amount) {
        CashRegister cashRegister = cashRegisterRepository.findById(cashRegisterId)
                .orElseThrow(() -> new ResourceNotFoundException("CashRegister", "id", cashRegisterId));

        if (cashRegister.getCashInDrawer().compareTo(amount) < 0) {
            throw new BadRequestException("Insufficient cash in drawer");
        }

        cashRegister.setCashInDrawer(cashRegister.getCashInDrawer().subtract(amount));
        cashRegister.setExpectedBalance(cashRegister.getExpectedBalance().subtract(amount));
        cashRegisterRepository.save(cashRegister);

        log.info("Removed {} from cash register {}", amount, cashRegister.getRegisterNumber());
    }

    @Transactional(readOnly = true)
    public CashRegister getCashRegisterById(Long id) {
        return cashRegisterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CashRegister", "id", id));
    }

    @Transactional(readOnly = true)
    public CashRegister getCashRegisterByNumber(String registerNumber) {
        return cashRegisterRepository.findByRegisterNumber(registerNumber)
                .orElseThrow(() -> new ResourceNotFoundException("CashRegister", "registerNumber", registerNumber));
    }

    @Transactional(readOnly = true)
    public List<CashRegister> getAllCashRegisters() {
        return cashRegisterRepository.findAllWithSalesAndUsers();
    }

    @Transactional(readOnly = true)
    public List<CashRegister> getOpenCashRegisters() {
        return cashRegisterRepository.findByIsOpenTrue();
    }

    @Transactional
    public void deactivateCashRegister(Long id) {
        CashRegister cashRegister = cashRegisterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CashRegister", "id", id));

        if (cashRegister.getIsOpen()) {
            throw new BadRequestException("Cannot deactivate an open cash register");
        }

        cashRegister.setActive(false);
        cashRegisterRepository.save(cashRegister);

        log.info("Cash register deactivated: {}", cashRegister.getRegisterNumber());
    }

    @Transactional
    public CashRegisterResponse toResponse(CashRegister cashRegister) {
        return CashRegisterResponse.builder()
                .id(cashRegister.getId())
                .registerNumber(cashRegister.getRegisterNumber())
                .name(cashRegister.getName())
                .location(cashRegister.getLocation())
                .isOpen(cashRegister.getIsOpen())
                .openingBalance(cashRegister.getOpeningBalance())
                .closingBalance(cashRegister.getClosingBalance())
                .expectedBalance(cashRegister.getExpectedBalance())
                .cashInDrawer(cashRegister.getCashInDrawer())
                .openedAt(cashRegister.getOpenedAt())
                .openedByUsername(cashRegister.getOpenedBy() != null ? cashRegister.getOpenedBy().getUsername() : null)
                .closedAt(cashRegister.getClosedAt())
                .closedByUsername(cashRegister.getClosedBy() != null ? cashRegister.getClosedBy().getUsername() : null)
                .active(cashRegister.getActive())
                .build();
    }
}