package com.djbc.dutyfree.util;

import com.djbc.dutyfree.domain.entity.Sale;
import com.djbc.dutyfree.domain.entity.SaleItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
@Slf4j
public class ReceiptGenerator {

    private static final int RECEIPT_WIDTH = 48;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /**
     * Generate receipt text
     */
    public String generateReceiptText(Sale sale, String receiptNumber) {
        StringBuilder receipt = new StringBuilder();

        // Header
        receipt.append(center("DUTY FREE")).append("\n");
        receipt.append(center("AEROPORT INTERNATIONAL DE OUAGADOUGOU")).append("\n");
        receipt.append(center("Burkina Faso")).append("\n");
        receipt.append(line()).append("\n");

        // Sale info
        receipt.append(leftRight("Date:", sale.getSaleDate().format(DATE_TIME_FORMATTER))).append("\n");
        receipt.append(leftRight("Ticket N°:", sale.getSaleNumber())).append("\n");
        receipt.append(leftRight("Receipt N°:", receiptNumber)).append("\n");
        receipt.append(leftRight("Cashier:", sale.getCashier().getFullName())).append("\n");
        receipt.append(leftRight("Register:", sale.getCashRegister().getRegisterNumber())).append("\n");

        if (sale.getCustomer() != null) {
            receipt.append(leftRight("Customer:",
                    sale.getCustomer().getFirstName() + " " + sale.getCustomer().getLastName())).append("\n");
        }

        receipt.append(line()).append("\n");

        // Items
        for (SaleItem item : sale.getItems()) {
            receipt.append(item.getProduct().getNameFr()).append("\n");
            receipt.append(leftRight(
                    String.format("  %d x %s", item.getQuantity(),
                            CurrencyUtil.formatXOF(item.getUnitPrice())),
                    CurrencyUtil.formatXOF(item.getTotalPrice())
            )).append("\n");

            if (item.getDiscount().compareTo(java.math.BigDecimal.ZERO) > 0) {
                receipt.append(leftRight("  Discount:",
                        "-" + CurrencyUtil.formatXOF(item.getDiscount()))).append("\n");
            }
        }

        receipt.append(line()).append("\n");

        // Totals
        receipt.append(leftRight("Subtotal:", CurrencyUtil.formatXOF(sale.getSubtotal()))).append("\n");

        if (sale.getDiscount().compareTo(java.math.BigDecimal.ZERO) > 0) {
            receipt.append(leftRight("Discount:",
                    "-" + CurrencyUtil.formatXOF(sale.getDiscount()))).append("\n");
        }

        receipt.append(leftRight("Tax:", CurrencyUtil.formatXOF(sale.getTaxAmount()))).append("\n");
        receipt.append(leftRight("TOTAL:", CurrencyUtil.formatXOF(sale.getTotalAmount()))).append("\n");
        receipt.append(line()).append("\n");

        // Payments
        receipt.append(center("PAYMENTS")).append("\n");
        sale.getPayments().forEach(payment -> {
            receipt.append(leftRight(
                    payment.getPaymentMethod().name() + ":",
                    CurrencyUtil.formatXOF(payment.getAmountInXOF())
            )).append("\n");
        });

        receipt.append(line()).append("\n");

        // Footer
        receipt.append(center("Merci pour votre visite")).append("\n");
        receipt.append(center("Thank you for your visit")).append("\n");
        receipt.append(center("A bientôt - See you soon")).append("\n");
        receipt.append(line()).append("\n");

        return receipt.toString();
    }

    private String center(String text) {
        int padding = (RECEIPT_WIDTH - text.length()) / 2;
        return " ".repeat(Math.max(0, padding)) + text;
    }

    private String leftRight(String left, String right) {
        int spaces = RECEIPT_WIDTH - left.length() - right.length();
        return left + " ".repeat(Math.max(0, spaces)) + right;
    }

    private String line() {
        return "=".repeat(RECEIPT_WIDTH);
    }
}