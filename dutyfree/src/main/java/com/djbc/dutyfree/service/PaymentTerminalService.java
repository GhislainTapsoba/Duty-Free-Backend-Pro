package com.djbc.dutyfree.service;

import com.djbc.dutyfree.domain.dto.request.PaymentTerminalRequest;
import com.djbc.dutyfree.domain.dto.request.TerminalPaymentRequest;
import com.djbc.dutyfree.domain.dto.response.PaymentTerminalResponse;
import com.djbc.dutyfree.domain.dto.response.TerminalTransactionResponse;
import com.djbc.dutyfree.domain.entity.*;
import com.djbc.dutyfree.exception.ResourceNotFoundException;
import com.djbc.dutyfree.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentTerminalService {

    private final PaymentTerminalRepository terminalRepository;
    private final TerminalTransactionRepository transactionRepository;
    private final CashRegisterRepository cashRegisterRepository;
    private final PaymentRepository paymentRepository;

    /**
     * Create a new payment terminal
     */
    @CacheEvict(value = "paymentTerminals", allEntries = true)
    public PaymentTerminalResponse createTerminal(PaymentTerminalRequest request) {
        log.info("Creating payment terminal: {}", request.getTerminalId());

        PaymentTerminal terminal = mapToEntity(request);
        terminal.setCreatedBy(getCurrentUsername());
        terminal.setUpdatedBy(getCurrentUsername());

        PaymentTerminal saved = terminalRepository.save(terminal);
        log.info("Payment terminal created: {}", saved.getId());

        return mapToResponse(saved);
    }

    /**
     * Update a payment terminal
     */
    @CacheEvict(value = "paymentTerminals", allEntries = true)
    public PaymentTerminalResponse updateTerminal(Long id, PaymentTerminalRequest request) {
        log.info("Updating payment terminal: {}", id);

        PaymentTerminal terminal = terminalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment terminal not found with id: " + id));

        updateEntityFromRequest(terminal, request);
        terminal.setUpdatedBy(getCurrentUsername());

        PaymentTerminal updated = terminalRepository.save(terminal);
        log.info("Payment terminal updated: {}", id);

        return mapToResponse(updated);
    }

    /**
     * Get terminal by ID
     */
    @Cacheable(value = "paymentTerminals", key = "#id")
    @Transactional(readOnly = true)
    public PaymentTerminalResponse getTerminalById(Long id) {
        log.debug("Fetching payment terminal by ID: {}", id);

        PaymentTerminal terminal = terminalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment terminal not found with id: " + id));

        return mapToResponse(terminal);
    }

    /**
     * Get terminal by terminal ID
     */
    @Transactional(readOnly = true)
    public PaymentTerminalResponse getTerminalByTerminalId(String terminalId) {
        log.debug("Fetching payment terminal by terminal ID: {}", terminalId);

        PaymentTerminal terminal = terminalRepository.findByTerminalId(terminalId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment terminal not found with terminal ID: " + terminalId));

        return mapToResponse(terminal);
    }

    /**
     * Get all active terminals
     */
    @Cacheable(value = "paymentTerminals", key = "'all_active'")
    @Transactional(readOnly = true)
    public List<PaymentTerminalResponse> getAllActiveTerminals() {
        log.debug("Fetching all active payment terminals");

        return terminalRepository.findAllActive().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get terminals by status
     */
    @Transactional(readOnly = true)
    public List<PaymentTerminalResponse> getTerminalsByStatus(PaymentTerminal.TerminalStatus status) {
        log.debug("Fetching payment terminals by status: {}", status);

        return terminalRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get terminals by cash register
     */
    @Transactional(readOnly = true)
    public List<PaymentTerminalResponse> getTerminalsByCashRegister(Long cashRegisterId) {
        log.debug("Fetching payment terminals for cash register: {}", cashRegisterId);

        return terminalRepository.findByCashRegisterId(cashRegisterId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get available terminals for payment
     */
    @Transactional(readOnly = true)
    public List<PaymentTerminalResponse> getAvailableTerminals(Long cashRegisterId) {
        log.debug("Fetching available payment terminals for cash register: {}", cashRegisterId);

        return terminalRepository.findAvailableTerminals(cashRegisterId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Process a payment through terminal
     */
    @CacheEvict(value = {"paymentTerminals", "terminalTransactions"}, allEntries = true)
    public TerminalTransactionResponse processPayment(TerminalPaymentRequest request) {
        log.info("Processing terminal payment: Terminal={}, Amount={}", request.getTerminalId(), request.getAmount());

        // Validate terminal
        PaymentTerminal terminal = terminalRepository.findById(request.getTerminalId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment terminal not found"));

        if (!terminal.isReady()) {
            throw new IllegalStateException("Terminal is not ready for transactions. Status: " + terminal.getStatus());
        }

        // Create transaction
        TerminalTransaction transaction = TerminalTransaction.builder()
                .transactionId(generateTransactionId())
                .terminal(terminal)
                .transactionType(TerminalTransaction.TransactionType.valueOf(request.getTransactionType()))
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(TerminalTransaction.TransactionStatus.PENDING)
                .signatureRequired(request.getRequireSignature())
                .startedAt(LocalDateTime.now())
                .notes(request.getNotes())
                .createdBy(getCurrentUsername())
                .build();

        // Link to payment if provided
        if (request.getPaymentId() != null) {
            Payment payment = paymentRepository.findById(request.getPaymentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
            transaction.setPayment(payment);
        }

        // Update terminal status
        terminal.setStatus(PaymentTerminal.TerminalStatus.BUSY);
        terminalRepository.save(terminal);

        // Simulate terminal communication (in real scenario, this would call terminal API/SDK)
        boolean success = simulateTerminalTransaction(terminal, transaction);

        if (success) {
            transaction.complete(TerminalTransaction.TransactionStatus.APPROVED);
            transaction.setAuthorizationCode(generateAuthCode());
            transaction.setReferenceNumber(UUID.randomUUID().toString());
            transaction.setCardType("VISA"); // Would come from terminal
            transaction.setCardNumberMasked("****1234"); // Would come from terminal
            transaction.setPinVerified(true);
        } else {
            transaction.complete(TerminalTransaction.TransactionStatus.DECLINED);
            transaction.setErrorCode("E001");
            transaction.setErrorMessage("Transaction declined by issuer");
        }

        // Update terminal status back to online
        terminal.setStatus(PaymentTerminal.TerminalStatus.ONLINE);
        terminal.setLastHeartbeat(LocalDateTime.now());
        terminalRepository.save(terminal);

        TerminalTransaction saved = transactionRepository.save(transaction);
        log.info("Terminal transaction completed: {} - Status: {}", saved.getTransactionId(), saved.getStatus());

        return mapTransactionToResponse(saved);
    }

    /**
     * Update terminal status (heartbeat)
     */
    @CacheEvict(value = "paymentTerminals", allEntries = true)
    public void updateTerminalHeartbeat(String terminalId, PaymentTerminal.TerminalStatus status) {
        log.debug("Updating heartbeat for terminal: {}", terminalId);

        PaymentTerminal terminal = terminalRepository.findByTerminalId(terminalId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment terminal not found"));

        terminal.setStatus(status);
        terminal.setLastHeartbeat(LocalDateTime.now());
        terminalRepository.save(terminal);
    }

    /**
     * Deactivate a terminal
     */
    @CacheEvict(value = "paymentTerminals", allEntries = true)
    public void deactivateTerminal(Long id) {
        log.info("Deactivating payment terminal: {}", id);

        PaymentTerminal terminal = terminalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment terminal not found"));

        terminal.setActive(false);
        terminal.setStatus(PaymentTerminal.TerminalStatus.OFFLINE);
        terminal.setUpdatedBy(getCurrentUsername());
        terminalRepository.save(terminal);
    }

    /**
     * Delete a terminal (soft delete)
     */
    @CacheEvict(value = "paymentTerminals", allEntries = true)
    public void deleteTerminal(Long id) {
        log.info("Deleting payment terminal: {}", id);

        PaymentTerminal terminal = terminalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment terminal not found"));

        terminal.setDeleted(true);
        terminal.setActive(false);
        terminal.setStatus(PaymentTerminal.TerminalStatus.OFFLINE);
        terminalRepository.save(terminal);
    }

    /**
     * Get transaction by ID
     */
    @Transactional(readOnly = true)
    public TerminalTransactionResponse getTransactionById(Long id) {
        TerminalTransaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Terminal transaction not found"));

        return mapTransactionToResponse(transaction);
    }

    /**
     * Get transactions by terminal
     */
    @Transactional(readOnly = true)
    public List<TerminalTransactionResponse> getTransactionsByTerminal(Long terminalId) {
        return transactionRepository.findByTerminalId(terminalId).stream()
                .map(this::mapTransactionToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get transactions by date range
     */
    @Transactional(readOnly = true)
    public List<TerminalTransactionResponse> getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.findByDateRange(startDate, endDate).stream()
                .map(this::mapTransactionToResponse)
                .collect(Collectors.toList());
    }

    // Helper methods

    private PaymentTerminal mapToEntity(PaymentTerminalRequest request) {
        PaymentTerminal terminal = PaymentTerminal.builder()
                .terminalId(request.getTerminalId())
                .name(request.getName())
                .manufacturer(request.getManufacturer())
                .model(request.getModel())
                .serialNumber(request.getSerialNumber())
                .terminalType(PaymentTerminal.TerminalType.valueOf(request.getTerminalType()))
                .connectionType(PaymentTerminal.ConnectionType.valueOf(request.getConnectionType()))
                .ipAddress(request.getIpAddress())
                .port(request.getPort())
                .comPort(request.getComPort())
                .merchantId(request.getMerchantId())
                .location(request.getLocation())
                .supportsContactless(request.getSupportsContactless())
                .supportsChip(request.getSupportsChip())
                .supportsMagneticStripe(request.getSupportsMagneticStripe())
                .supportsMobilePayment(request.getSupportsMobilePayment())
                .firmwareVersion(request.getFirmwareVersion())
                .active(request.getActive())
                .notes(request.getNotes())
                .build();

        if (request.getCashRegisterId() != null) {
            CashRegister cashRegister = cashRegisterRepository.findById(request.getCashRegisterId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cash register not found"));
            terminal.setCashRegister(cashRegister);
        }

        return terminal;
    }

    private void updateEntityFromRequest(PaymentTerminal terminal, PaymentTerminalRequest request) {
        terminal.setTerminalId(request.getTerminalId());
        terminal.setName(request.getName());
        terminal.setManufacturer(request.getManufacturer());
        terminal.setModel(request.getModel());
        terminal.setSerialNumber(request.getSerialNumber());
        terminal.setTerminalType(PaymentTerminal.TerminalType.valueOf(request.getTerminalType()));
        terminal.setConnectionType(PaymentTerminal.ConnectionType.valueOf(request.getConnectionType()));
        terminal.setIpAddress(request.getIpAddress());
        terminal.setPort(request.getPort());
        terminal.setComPort(request.getComPort());
        terminal.setMerchantId(request.getMerchantId());
        terminal.setLocation(request.getLocation());
        terminal.setSupportsContactless(request.getSupportsContactless());
        terminal.setSupportsChip(request.getSupportsChip());
        terminal.setSupportsMagneticStripe(request.getSupportsMagneticStripe());
        terminal.setSupportsMobilePayment(request.getSupportsMobilePayment());
        terminal.setFirmwareVersion(request.getFirmwareVersion());
        terminal.setActive(request.getActive());
        terminal.setNotes(request.getNotes());

        if (request.getCashRegisterId() != null) {
            CashRegister cashRegister = cashRegisterRepository.findById(request.getCashRegisterId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cash register not found"));
            terminal.setCashRegister(cashRegister);
        }
    }

    private PaymentTerminalResponse mapToResponse(PaymentTerminal terminal) {
        PaymentTerminalResponse.PaymentTerminalResponseBuilder builder = PaymentTerminalResponse.builder()
                .id(terminal.getId())
                .terminalId(terminal.getTerminalId())
                .name(terminal.getName())
                .manufacturer(terminal.getManufacturer())
                .model(terminal.getModel())
                .serialNumber(terminal.getSerialNumber())
                .terminalType(terminal.getTerminalType().name())
                .connectionType(terminal.getConnectionType().name())
                .ipAddress(terminal.getIpAddress())
                .port(terminal.getPort())
                .comPort(terminal.getComPort())
                .merchantId(terminal.getMerchantId())
                .location(terminal.getLocation())
                .supportsContactless(terminal.getSupportsContactless())
                .supportsChip(terminal.getSupportsChip())
                .supportsMagneticStripe(terminal.getSupportsMagneticStripe())
                .supportsMobilePayment(terminal.getSupportsMobilePayment())
                .status(terminal.getStatus().name())
                .lastHeartbeat(terminal.getLastHeartbeat())
                .firmwareVersion(terminal.getFirmwareVersion())
                .active(terminal.getActive())
                .createdAt(terminal.getCreatedAt())
                .updatedAt(terminal.getUpdatedAt())
                .createdBy(terminal.getCreatedBy())
                .updatedBy(terminal.getUpdatedBy())
                .notes(terminal.getNotes())
                .ready(terminal.isReady());

        if (terminal.getCashRegister() != null) {
            builder.cashRegisterId(terminal.getCashRegister().getId())
                    .cashRegisterName(terminal.getCashRegister().getName());
        }

        if (terminal.getLastHeartbeat() != null) {
            long seconds = ChronoUnit.SECONDS.between(terminal.getLastHeartbeat(), LocalDateTime.now());
            builder.secondsSinceLastHeartbeat(seconds);
        }

        return builder.build();
    }

    private TerminalTransactionResponse mapTransactionToResponse(TerminalTransaction transaction) {
        return TerminalTransactionResponse.builder()
                .id(transaction.getId())
                .transactionId(transaction.getTransactionId())
                .terminalId(transaction.getTerminal().getId())
                .terminalName(transaction.getTerminal().getName())
                .paymentId(transaction.getPayment() != null ? transaction.getPayment().getId() : null)
                .transactionType(transaction.getTransactionType().name())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .status(transaction.getStatus().name())
                .cardType(transaction.getCardType())
                .cardNumberMasked(transaction.getCardNumberMasked())
                .cardHolderName(transaction.getCardHolderName())
                .authorizationCode(transaction.getAuthorizationCode())
                .referenceNumber(transaction.getReferenceNumber())
                .acquirerId(transaction.getAcquirerId())
                .merchantId(transaction.getMerchantId())
                .terminalReceipt(transaction.getTerminalReceipt())
                .customerReceipt(transaction.getCustomerReceipt())
                .errorCode(transaction.getErrorCode())
                .errorMessage(transaction.getErrorMessage())
                .startedAt(transaction.getStartedAt())
                .completedAt(transaction.getCompletedAt())
                .responseTimeMs(transaction.getResponseTimeMs())
                .signatureRequired(transaction.getSignatureRequired())
                .signatureVerified(transaction.getSignatureVerified())
                .pinVerified(transaction.getPinVerified())
                .createdAt(transaction.getCreatedAt())
                .createdBy(transaction.getCreatedBy())
                .notes(transaction.getNotes())
                .successful(transaction.isSuccessful())
                .canBeReversed(transaction.canBeReversed())
                .build();
    }

    private String generateTransactionId() {
        return "TPE-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String generateAuthCode() {
        return "AUTH-" + (100000 + (int) (Math.random() * 900000));
    }

    /**
     * Simulate terminal transaction
     * In real implementation, this would communicate with actual terminal hardware/API
     */
    private boolean simulateTerminalTransaction(PaymentTerminal terminal, TerminalTransaction transaction) {
        log.info("Simulating transaction on terminal: {}", terminal.getTerminalId());

        try {
            // Simulate processing time
            Thread.sleep(1000 + (long) (Math.random() * 2000));

            // 95% success rate in simulation
            return Math.random() < 0.95;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "system";
    }
}
