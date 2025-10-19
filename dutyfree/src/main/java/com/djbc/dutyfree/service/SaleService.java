package com.djbc.dutyfree.service;

import com.djbc.dutyfree.domain.dto.request.SaleRequest;
import com.djbc.dutyfree.domain.dto.response.SaleResponse;
import com.djbc.dutyfree.domain.entity.*;
import com.djbc.dutyfree.domain.enums.SaleStatus;
import com.djbc.dutyfree.exception.BadRequestException;
import com.djbc.dutyfree.exception.ResourceNotFoundException;
import com.djbc.dutyfree.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SaleService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final CashRegisterRepository cashRegisterRepository;
    private final UserRepository userRepository;
    private final StockService stockService;
    private final PaymentService paymentService;
    private final ReceiptService receiptService;
    private final AuthService authService;

    @Transactional
    public SaleResponse createSale(SaleRequest request) {
        // Validate cash register
        CashRegister cashRegister = cashRegisterRepository.findById(request.getCashRegisterId())
                .orElseThrow(() -> new ResourceNotFoundException("CashRegister", "id", request.getCashRegisterId()));

        if (!cashRegister.getIsOpen()) {
            throw new BadRequestException("Cash register is not open");
        }

        // Get current user (cashier)
        User cashier = authService.getCurrentUser();

        // Get customer if provided
        Customer customer = null;
        if (request.getCustomerId() != null) {
            customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", request.getCustomerId()));
        }

        // Create sale
        Sale sale = Sale.builder()
                .saleNumber(generateSaleNumber())
                .saleDate(LocalDateTime.now())
                .cashier(cashier)
                .customer(customer)
                .cashRegister(cashRegister)
                .status(SaleStatus.PENDING)
                .subtotal(BigDecimal.ZERO)
                .discount(request.getDiscount() != null ? request.getDiscount() : BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .totalAmount(BigDecimal.ZERO)
                .notes(request.getNotes())
                .passengerName(request.getPassengerName())
                .flightNumber(request.getFlightNumber())
                .airline(request.getAirline())
                .destination(request.getDestination())
                .items(new ArrayList<>())
                .payments(new ArrayList<>())
                .build();

        // Process sale items
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;

        for (SaleRequest.SaleItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", itemRequest.getProductId()));

            // Check stock availability
            if (product.getTrackStock()) {
                Integer available = stockService.getAvailableStock(product.getId());
                if (available < itemRequest.getQuantity()) {
                    throw new BadRequestException("Insufficient stock for product: " + product.getNameFr());
                }
                // Reserve stock
                stockService.reserveStock(product.getId(), itemRequest.getQuantity());
            }

            BigDecimal unitPrice = product.getSellingPriceXOF();
            BigDecimal itemDiscount = itemRequest.getDiscount() != null ? itemRequest.getDiscount() : BigDecimal.ZERO;
            BigDecimal taxRate = product.getTaxRate();

            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            lineTotal = lineTotal.subtract(itemDiscount);

            BigDecimal taxAmount = lineTotal.multiply(taxRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal totalPrice = lineTotal.add(taxAmount);

            SaleItem saleItem = SaleItem.builder()
                    .sale(sale)
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(unitPrice)
                    .discount(itemDiscount)
                    .taxRate(taxRate)
                    .taxAmount(taxAmount)
                    .totalPrice(totalPrice)
                    .build();

            sale.getItems().add(saleItem);
            subtotal = subtotal.add(lineTotal);
            totalTax = totalTax.add(taxAmount);
        }

        // Apply overall discount
        subtotal = subtotal.subtract(sale.getDiscount());

        sale.setSubtotal(subtotal);
        sale.setTaxAmount(totalTax);
        sale.setTotalAmount(subtotal.add(totalTax));

        // Save sale
        sale = saleRepository.save(sale);

        // Process payments if provided
        if (request.getPayments() != null && !request.getPayments().isEmpty()) {
            for (var paymentRequest : request.getPayments()) {
                paymentService.processPayment(sale.getId(), paymentRequest);
            }

            // Complete sale if fully paid
            BigDecimal totalPaid = paymentService.getTotalPaidAmount(sale.getId());
            if (totalPaid.compareTo(sale.getTotalAmount()) >= 0) {
                sale.setStatus(SaleStatus.COMPLETED);

                // Reduce stock for completed sale
                for (SaleItem item : sale.getItems()) {
                    if (item.getProduct().getTrackStock()) {
                        stockService.reduceStock(item.getProduct().getId(), item.getQuantity());
                    }
                }

                // Generate receipt
                receiptService.generateReceipt(sale.getId());

                sale = saleRepository.save(sale);
            }
        }

        log.info("Sale created: {}", sale.getSaleNumber());

        return mapToResponse(sale);
    }

    @Transactional
    public SaleResponse completeSale(Long saleId) {
        Sale sale = saleRepository.findByIdWithDetails(saleId)
                .orElseThrow(() -> new ResourceNotFoundException("Sale", "id", saleId));

        if (sale.getStatus() == SaleStatus.COMPLETED) {
            throw new BadRequestException("Sale is already completed");
        }

        BigDecimal totalPaid = paymentService.getTotalPaidAmount(saleId);
        if (totalPaid.compareTo(sale.getTotalAmount()) < 0) {
            throw new BadRequestException("Sale is not fully paid");
        }

        sale.setStatus(SaleStatus.COMPLETED);

        // Reduce stock
        for (SaleItem item : sale.getItems()) {
            if (item.getProduct().getTrackStock()) {
                stockService.reduceStock(item.getProduct().getId(), item.getQuantity());
            }
        }

        // Generate receipt if not already generated
        if (sale.getReceipt() == null) {
            receiptService.generateReceipt(saleId);
        }

        sale = saleRepository.save(sale);
        log.info("Sale completed: {}", sale.getSaleNumber());

        return mapToResponse(sale);
    }

    @Transactional
    public void cancelSale(Long saleId, String reason) {
        Sale sale = saleRepository.findByIdWithDetails(saleId)
                .orElseThrow(() -> new ResourceNotFoundException("Sale", "id", saleId));

        if (sale.getStatus() == SaleStatus.COMPLETED) {
            throw new BadRequestException("Cannot cancel completed sale");
        }

        // Release reserved stock
        for (SaleItem item : sale.getItems()) {
            if (item.getProduct().getTrackStock()) {
                stockService.releaseReservedStock(item.getProduct().getId(), item.getQuantity());
            }
        }

        sale.setStatus(SaleStatus.CANCELLED);
        sale.setNotes(sale.getNotes() != null ? sale.getNotes() + "\nCancellation reason: " + reason : "Cancellation reason: " + reason);
        saleRepository.save(sale);

        log.info("Sale cancelled: {}", sale.getSaleNumber());
    }

    @Transactional(readOnly = true)
    public SaleResponse getSaleById(Long id) {
        Sale sale = saleRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale", "id", id));
        return mapToResponse(sale);
    }

    @Transactional(readOnly = true)
    public SaleResponse getSaleBySaleNumber(String saleNumber) {
        Sale sale = saleRepository.findBySaleNumber(saleNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Sale", "saleNumber", saleNumber));
        return mapToResponse(sale);
    }

    @Transactional(readOnly = true)
    public Page<SaleResponse> getSalesByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return saleRepository.findBySaleDateBetween(startDate, endDate, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public List<SaleResponse> getSalesByCashier(Long cashierId, LocalDateTime startDate, LocalDateTime endDate) {
        return saleRepository.findByCashierAndDateBetween(cashierId, startDate, endDate).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SaleResponse> getSalesByCashRegister(Long cashRegisterId, LocalDateTime startDate, LocalDateTime endDate) {
        return saleRepository.findByCashRegisterAndDateBetween(cashRegisterId, startDate, endDate).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private String generateSaleNumber() {
        LocalDateTime now = LocalDateTime.now();
        String prefix = String.format("SAL-%d%02d%02d-", now.getYear(), now.getMonthValue(), now.getDayOfMonth());
        Long count = saleRepository.count() + 1;
        return prefix + String.format("%06d", count);
    }

    private SaleResponse mapToResponse(Sale sale) {
        List<SaleResponse.SaleItemResponse> items = sale.getItems().stream()
                .map(item -> SaleResponse.SaleItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getNameFr())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .discount(item.getDiscount())
                        .taxRate(item.getTaxRate())
                        .taxAmount(item.getTaxAmount())
                        .totalPrice(item.getTotalPrice())
                        .build())
                .collect(Collectors.toList());

        List<SaleResponse.PaymentResponse> payments = sale.getPayments().stream()
                .map(payment -> SaleResponse.PaymentResponse.builder()
                        .id(payment.getId())
                        .paymentMethod(payment.getPaymentMethod().name())
                        .currency(payment.getCurrency().name())
                        .amount(payment.getAmountInCurrency())
                        .amountInXOF(payment.getAmountInXOF())
                        .paymentDate(payment.getPaymentDate())
                        .transactionReference(payment.getTransactionReference())
                        .build())
                .collect(Collectors.toList());

        return SaleResponse.builder()
                .id(sale.getId())
                .saleNumber(sale.getSaleNumber())
                .saleDate(sale.getSaleDate())
                .cashierName(sale.getCashier().getFullName())
                .customerName(sale.getCustomer() != null ?
                        sale.getCustomer().getFirstName() + " " + sale.getCustomer().getLastName() : null)
                .cashRegisterNumber(sale.getCashRegister().getRegisterNumber())
                .status(sale.getStatus())
                .subtotal(sale.getSubtotal())
                .discount(sale.getDiscount())
                .taxAmount(sale.getTaxAmount())
                .totalAmount(sale.getTotalAmount())
                .notes(sale.getNotes())
                .items(items)
                .payments(payments)
                .receiptNumber(sale.getReceipt() != null ? sale.getReceipt().getReceiptNumber() : null)
                .passengerName(sale.getPassengerName())
                .flightNumber(sale.getFlightNumber())
                .airline(sale.getAirline())
                .destination(sale.getDestination())
                .build();
    }
}