package com.djbc.dutyfree.service;

import com.djbc.dutyfree.domain.dto.request.PaymentRequest;
import com.djbc.dutyfree.domain.entity.Payment;
import com.djbc.dutyfree.domain.entity.Sale;
import com.djbc.dutyfree.domain.enums.Currency;
import com.djbc.dutyfree.exception.BadRequestException;
import com.djbc.dutyfree.exception.ResourceNotFoundException;
import com.djbc.dutyfree.repository.PaymentRepository;
import com.djbc.dutyfree.repository.SaleRepository;
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
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final SaleRepository saleRepository;
    private final ExchangeRateService exchangeRateService;

    @Transactional
    public Payment processPayment(Long saleId, PaymentRequest request) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new ResourceNotFoundException("Sale", "id", saleId));

        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Payment amount must be greater than 0");
        }

        // Convert amount to XOF if different currency
        BigDecimal amountInXOF;
        BigDecimal exchangeRate = BigDecimal.ONE;

        if (request.getCurrency() != Currency.XOF) {
            exchangeRate = exchangeRateService.getExchangeRate(request.getCurrency());
            amountInXOF = request.getAmount().multiply(exchangeRate);
        } else {
            amountInXOF = request.getAmount();
        }

        Payment payment = Payment.builder()
                .sale(sale)
                .paymentMethod(request.getPaymentMethod())
                .currency(request.getCurrency())
                .amountInCurrency(request.getAmount())
                .amountInXOF(amountInXOF)
                .exchangeRate(exchangeRate)
                .paymentDate(LocalDateTime.now())
                .transactionReference(request.getTransactionReference())
                .cardLast4Digits(request.getCardLast4Digits())
                .cardType(request.getCardType())
                .mobileMoneyProvider(request.getMobileMoneyProvider())
                .mobileMoneyNumber(request.getMobileMoneyNumber())
                .notes(request.getNotes())
                .verified(false)
                .build();

        payment = paymentRepository.save(payment);
        log.info("Payment processed for sale {}: {} {}", sale.getSaleNumber(),
                request.getAmount(), request.getCurrency());

        return payment;
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalPaidAmount(Long saleId) {
        List<Payment> payments = paymentRepository.findBySaleId(saleId);
        return payments.stream()
                .map(Payment::getAmountInXOF)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional(readOnly = true)
    public List<Payment> getPaymentsBySale(Long saleId) {
        return paymentRepository.findBySaleId(saleId);
    }

    @Transactional
    public Payment verifyPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));

        payment.setVerified(true);
        payment = paymentRepository.save(payment);

        log.info("Payment verified: {}", paymentId);
        return payment;
    }
}