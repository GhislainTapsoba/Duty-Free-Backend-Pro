package com.djbc.dutyfree.service;

import com.djbc.dutyfree.domain.entity.Receipt;
import com.djbc.dutyfree.domain.entity.Sale;
import com.djbc.dutyfree.domain.entity.SaleItem;
import com.djbc.dutyfree.exception.ResourceNotFoundException;
import com.djbc.dutyfree.repository.ReceiptRepository;
import com.djbc.dutyfree.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReceiptService {

    private final ReceiptRepository receiptRepository;
    private final SaleRepository saleRepository;

    @Transactional
    public Receipt generateReceipt(Long saleId) {
        Sale sale = saleRepository.findByIdWithDetails(saleId)
                .orElseThrow(() -> new ResourceNotFoundException("Sale", "id", saleId));

        // Check if receipt already exists
        if (sale.getReceipt() != null) {
            return sale.getReceipt();
        }

        String receiptNumber = generateReceiptNumber();
        String receiptContent = buildReceiptContent(sale);

        Receipt receipt = Receipt.builder()
                .sale(sale)
                .receiptNumber(receiptNumber)
                .printedDate(LocalDateTime.now())
                .receiptContent(receiptContent)
                .printed(false)
                .emailed(false)
                .headerMessage("Bienvenue - Welcome")
                .footerMessage("Merci pour votre visite - Thank you for your visit")
                .build();

        receipt = receiptRepository.save(receipt);
        log.info("Receipt generated: {}", receiptNumber);

        return receipt;
    }

    @Transactional
    public Receipt printReceipt(Long receiptId) {
        Receipt receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt", "id", receiptId));

        receipt.setPrinted(true);
        receipt = receiptRepository.save(receipt);

        log.info("Receipt printed: {}", receipt.getReceiptNumber());
        return receipt;
    }

    @Transactional
    public Receipt emailReceipt(Long receiptId, String emailAddress) {
        Receipt receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt", "id", receiptId));

        // TODO: Implement email sending logic

        receipt.setEmailed(true);
        receipt.setEmailAddress(emailAddress);
        receipt.setEmailedDate(LocalDateTime.now());
        receipt = receiptRepository.save(receipt);

        log.info("Receipt emailed to {}: {}", emailAddress, receipt.getReceiptNumber());
        return receipt;
    }

    @Transactional(readOnly = true)
    public Receipt getReceiptBySale(Long saleId) {
        return receiptRepository.findBySaleId(saleId)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt not found for sale: " + saleId));
    }

    private String generateReceiptNumber() {
        LocalDateTime now = LocalDateTime.now();
        String prefix = String.format("REC-%d%02d%02d-", now.getYear(), now.getMonthValue(), now.getDayOfMonth());
        Long count = receiptRepository.count() + 1;
        return prefix + String.format("%06d", count);
    }

    private String buildReceiptContent(Sale sale) {
        StringBuilder content = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        // Header
        content.append("=====================================\n");
        content.append("    DUTY FREE - AEROPORT DE OUA\n");
        content.append("=====================================\n");
        content.append("Date: ").append(sale.getSaleDate().format(formatter)).append("\n");
        content.append("Ticket N°: ").append(sale.getSaleNumber()).append("\n");
        content.append("Receipt N°: ").append(sale.getReceipt() != null ?
                sale.getReceipt().getReceiptNumber() : "N/A").append("\n");
        content.append("Cashier: ").append(sale.getCashier().getFullName()).append("\n");
        content.append("Register: ").append(sale.getCashRegister().getRegisterNumber()).append("\n");
        content.append("-------------------------------------\n");

        // Items
        for (SaleItem item : sale.getItems()) {
            content.append(item.getProduct().getNameFr()).append("\n");
            content.append("  ").append(item.getQuantity()).append(" x ")
                    .append(String.format("%,.0f", item.getUnitPrice())).append(" FCFA\n");
            if (item.getDiscount().compareTo(BigDecimal.ZERO) > 0) {
                content.append("  Discount: -").append(String.format("%,.0f", item.getDiscount())).append(" FCFA\n");
            }
            content.append("  Total: ").append(String.format("%,.0f", item.getTotalPrice())).append(" FCFA\n");
        }

        content.append("-------------------------------------\n");

        // Totals
        content.append("Subtotal: ").append(String.format("%,.0f", sale.getSubtotal())).append(" FCFA\n");
        if (sale.getDiscount().compareTo(BigDecimal.ZERO) > 0) {
            content.append("Discount: -").append(String.format("%,.0f", sale.getDiscount())).append(" FCFA\n");
        }
        content.append("Tax: ").append(String.format("%,.0f", sale.getTaxAmount())).append(" FCFA\n");
        content.append("TOTAL TTC: ").append(String.format("%,.0f", sale.getTotalAmount())).append(" FCFA\n");
        content.append("-------------------------------------\n");

        // Payments
        content.append("PAYMENTS:\n");
        sale.getPayments().forEach(payment -> {
            content.append(payment.getPaymentMethod()).append(": ")
                    .append(String.format("%,.0f", payment.getAmountInXOF())).append(" FCFA\n");
        });

        // Footer
        content.append("=====================================\n");
        content.append("   Merci - Thank you\n");
        content.append("   Au revoir - Goodbye\n");
        content.append("=====================================\n");

        return content.toString();
    }
}