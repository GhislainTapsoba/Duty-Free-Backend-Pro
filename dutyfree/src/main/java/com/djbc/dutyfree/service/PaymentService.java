package com.djbc.dutyfree.service;

import com.djbc.dutyfree.domain.dto.request.PaymentRequest;
import com.djbc.dutyfree.domain.entity.Payment;
import com.djbc.dutyfree.domain.entity.Sale;
import com.djbc.dutyfree.repository.PaymentRepository;
import com.djbc.dutyfree.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final SaleRepository saleRepository;

    public Payment processPayment(Long saleId, PaymentRequest request) {
        Sale sale = saleRepository.findById(saleId).orElseThrow();
        
        Payment payment = Payment.builder()
                .sale(sale)
                .paymentMethod(request.getPaymentMethod())
                .currency(request.getCurrency())
                .amountInCurrency(request.getAmount())
                .amountInXOF(request.getAmount()) // Simplified - no conversion
                .paymentDate(LocalDateTime.now())
                .transactionReference(request.getTransactionReference())
                .verified(true)
                .build();
                
        return paymentRepository.save(payment);
    }

    public BigDecimal getTotalPaidAmount(Long saleId) {
        List<Payment> payments = paymentRepository.findBySaleId(saleId);
        return payments.stream()
                .map(Payment::getAmountInXOF)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<Payment> getPaymentsBySale(Long saleId) {
        return paymentRepository.findBySaleId(saleId);
    }

    public Payment verifyPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId).orElseThrow();
        payment.setVerified(true);
        return paymentRepository.save(payment);
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }
}