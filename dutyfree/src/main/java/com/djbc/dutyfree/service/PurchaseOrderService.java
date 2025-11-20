package com.djbc.dutyfree.service;

import com.djbc.dutyfree.domain.dto.request.PurchaseOrderRequest;
import com.djbc.dutyfree.domain.entity.*;
import com.djbc.dutyfree.domain.enums.OrderStatus;
import com.djbc.dutyfree.domain.enums.SommierStatus;
import com.djbc.dutyfree.exception.BadRequestException;
import com.djbc.dutyfree.exception.ResourceNotFoundException;
import com.djbc.dutyfree.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final SommierRepository sommierRepository;
    private final StockService stockService;

    @Transactional
    public PurchaseOrder createPurchaseOrder(PurchaseOrderRequest request) {
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", request.getSupplierId()));

        PurchaseOrder order = PurchaseOrder.builder()
                .orderNumber(generateOrderNumber())
                .supplier(supplier)
                .orderDate(request.getOrderDate())
                .expectedDeliveryDate(request.getExpectedDeliveryDate())
                .status(OrderStatus.DRAFT)
                .subtotal(BigDecimal.ZERO)
                .transportCost(request.getTransportCost() != null ? request.getTransportCost() : BigDecimal.ZERO)
                .insuranceCost(request.getInsuranceCost() != null ? request.getInsuranceCost() : BigDecimal.ZERO)
                .otherCosts(request.getOtherCosts() != null ? request.getOtherCosts() : BigDecimal.ZERO)
                .totalCost(BigDecimal.ZERO)
                .notes(request.getNotes())
                .items(new ArrayList<>())
                .build();

        // Process order items
        BigDecimal subtotal = BigDecimal.ZERO;

        for (PurchaseOrderRequest.PurchaseOrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", itemRequest.getProductId()));

            BigDecimal totalPrice = itemRequest.getUnitPrice()
                    .multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

            PurchaseOrderItem orderItem = PurchaseOrderItem.builder()
                    .purchaseOrder(order)
                    .product(product)
                    .quantityOrdered(itemRequest.getQuantity())
                    .quantityReceived(0)
                    .unitPrice(itemRequest.getUnitPrice())
                    .totalPrice(totalPrice)
                    .notes(itemRequest.getNotes())
                    .build();

            order.getItems().add(orderItem);
            subtotal = subtotal.add(totalPrice);
        }

        order.setSubtotal(subtotal);

        // Calculate total cost
        BigDecimal totalCost = subtotal
                .add(order.getTransportCost())
                .add(order.getInsuranceCost())
                .add(order.getOtherCosts());
        order.setTotalCost(totalCost);

        order = purchaseOrderRepository.save(order);
        log.info("Purchase order created: {}", order.getOrderNumber());

        return order;
    }

    @Transactional
    public PurchaseOrder updatePurchaseOrder(Long id, PurchaseOrderRequest request) {
        PurchaseOrder order = purchaseOrderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", "id", id));

        if (order.getStatus() == OrderStatus.RECEIVED) {
            throw new BadRequestException("Cannot update received order");
        }

        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", request.getSupplierId()));

        order.setSupplier(supplier);
        order.setOrderDate(request.getOrderDate());
        order.setExpectedDeliveryDate(request.getExpectedDeliveryDate());
        order.setTransportCost(request.getTransportCost());
        order.setInsuranceCost(request.getInsuranceCost());
        order.setOtherCosts(request.getOtherCosts());
        order.setNotes(request.getNotes());

        // Clear and re-add items
        order.getItems().clear();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (PurchaseOrderRequest.PurchaseOrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", itemRequest.getProductId()));

            BigDecimal totalPrice = itemRequest.getUnitPrice()
                    .multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

            PurchaseOrderItem orderItem = PurchaseOrderItem.builder()
                    .purchaseOrder(order)
                    .product(product)
                    .quantityOrdered(itemRequest.getQuantity())
                    .quantityReceived(0)
                    .unitPrice(itemRequest.getUnitPrice())
                    .totalPrice(totalPrice)
                    .notes(itemRequest.getNotes())
                    .build();

            order.getItems().add(orderItem);
            subtotal = subtotal.add(totalPrice);
        }

        order.setSubtotal(subtotal);
        BigDecimal totalCost = subtotal
                .add(order.getTransportCost())
                .add(order.getInsuranceCost())
                .add(order.getOtherCosts());
        order.setTotalCost(totalCost);

        order = purchaseOrderRepository.save(order);
        log.info("Purchase order updated: {}", order.getOrderNumber());

        return order;
    }

    @Transactional
    public PurchaseOrder confirmOrder(Long id) {
        PurchaseOrder order = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", "id", id));

        if (order.getStatus() != OrderStatus.DRAFT && order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("Only draft or pending orders can be confirmed");
        }

        order.setStatus(OrderStatus.CONFIRMED);
        order = purchaseOrderRepository.save(order);

        log.info("Purchase order confirmed: {}", order.getOrderNumber());
        return order;
    }

    @Transactional
    public PurchaseOrder receiveOrder(Long id, String sommierNumber, String location) {
        PurchaseOrder order = purchaseOrderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", "id", id));

        if (order.getStatus() == OrderStatus.RECEIVED) {
            throw new BadRequestException("Order already received");
        }

        // Create Sommier
        Sommier sommier = Sommier.builder()
                .sommierNumber(sommierNumber)
                .purchaseOrder(order)
                .openingDate(LocalDate.now())
                .initialValue(order.getTotalCost())
                .currentValue(order.getTotalCost())
                .clearedValue(BigDecimal.ZERO)
                .status(SommierStatus.ACTIVE)
                .build();

        sommier = sommierRepository.save(sommier);

        // Add stock for each item
        for (PurchaseOrderItem item : order.getItems()) {
            stockService.addStock(
                    item.getProduct().getId(),
                    sommier.getId(),
                    item.getQuantityOrdered(),
                    location,
                    null,
                    null
            );

            item.setQuantityReceived(item.getQuantityOrdered());
        }

        order.setStatus(OrderStatus.RECEIVED);
        order.setReceivedDate(LocalDate.now());
        order = purchaseOrderRepository.save(order);

        log.info("Purchase order received: {}, Sommier: {}", order.getOrderNumber(), sommierNumber);
        return order;
    }

    @Transactional
    public void cancelOrder(Long id, String reason) {
        PurchaseOrder order = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", "id", id));

        if (order.getStatus() == OrderStatus.RECEIVED) {
            throw new BadRequestException("Cannot cancel received order");
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setNotes(order.getNotes() != null ?
                order.getNotes() + "\nCancellation reason: " + reason :
                "Cancellation reason: " + reason);
        purchaseOrderRepository.save(order);

        log.info("Purchase order cancelled: {}", order.getOrderNumber());
    }

    @Transactional(readOnly = true)
    public PurchaseOrder getOrderById(Long id) {
        return purchaseOrderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", "id", id));
    }

    @Transactional(readOnly = true)
    public PurchaseOrder getOrderByNumber(String orderNumber) {
        return purchaseOrderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", "orderNumber", orderNumber));
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrder> getOrdersBySupplier(Long supplierId) {
        return purchaseOrderRepository.findBySupplier_Id(supplierId);
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrder> getOrdersByStatus(OrderStatus status) {
        return purchaseOrderRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrder> getOverdueOrders() {
        return purchaseOrderRepository.findOverdueOrders(LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrder> getAllPurchaseOrders() {
        return purchaseOrderRepository.findAllActive();
    }

    private String generateOrderNumber() {
        LocalDate now = LocalDate.now();
        String prefix = String.format("PO-%d%02d%02d-", now.getYear(), now.getMonthValue(), now.getDayOfMonth());
        Long count = purchaseOrderRepository.count() + 1;
        return prefix + String.format("%06d", count);
    }
}