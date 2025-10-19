package com.djbc.dutyfree.service;

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
    public CashRegister createCashRegister(String registerNumber, String name, String location) {
        if (cashRegisterRepository.existsByRegisterNumber(registerNumber)) {
            throw new BadRequestException("Cash register with number " + registerNumber + " already exists");
        }

        CashRegister cashRegister = CashRegister.builder()
                .registerNumber(registerNumber)
                .name(name)
                .location(location)
                .active(true)
                .isOpen(false)
                .openingBalance(BigDecimal.ZERO)
                .closingBalance(BigDecimal.ZERO)
                .expectedBalance(BigDecimal.ZERO)
                .cashInDrawer(BigDecimal.ZERO)
                .build();

        cashRegister = cashRegisterRepository.save(cashRegister);
        log.info("Cash register created: {}", registerNumber);

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
        return cashRegisterRepository.findByActiveTrue();
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
}