package com.djbc.dutyfree.controller;

import com.djbc.dutyfree.domain.dto.request.PurchaseOrderRequest;
import com.djbc.dutyfree.domain.dto.response.ApiResponse;
import com.djbc.dutyfree.domain.entity.PurchaseOrder;
import com.djbc.dutyfree.domain.enums.OrderStatus;
import com.djbc.dutyfree.service.PurchaseOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Purchase Orders", description = "Purchase order management APIs")
@PreAuthorize("hasAnyRole('ADMIN', 'STOCK_MANAGER')")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @PostMapping
    @Operation(summary = "Create purchase order", description = "Create a new purchase order")
    public ResponseEntity<ApiResponse<PurchaseOrder>> createPurchaseOrder(
            @Valid @RequestBody PurchaseOrderRequest request) {
        PurchaseOrder order = purchaseOrderService.createPurchaseOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Purchase order created successfully", order));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update purchase order", description = "Update existing purchase order")
    public ResponseEntity<ApiResponse<PurchaseOrder>> updatePurchaseOrder(
            @PathVariable Long id,
            @Valid @RequestBody PurchaseOrderRequest request) {
        PurchaseOrder order = purchaseOrderService.updatePurchaseOrder(id, request);
        return ResponseEntity.ok(ApiResponse.success("Purchase order updated successfully", order));
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "Confirm order", description = "Confirm a purchase order")
    public ResponseEntity<ApiResponse<PurchaseOrder>> confirmOrder(@PathVariable Long id) {
        PurchaseOrder order = purchaseOrderService.confirmOrder(id);
        return ResponseEntity.ok(ApiResponse.success("Purchase order confirmed successfully", order));
    }

    @PostMapping("/{id}/receive")
    @Operation(summary = "Receive order", description = "Mark order as received and create sommier")
    public ResponseEntity<ApiResponse<PurchaseOrder>> receiveOrder(
            @PathVariable Long id,
            @RequestParam String sommierNumber,
            @RequestParam String location) {
        PurchaseOrder order = purchaseOrderService.receiveOrder(id, sommierNumber, location);
        return ResponseEntity.ok(ApiResponse.success("Purchase order received successfully", order));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel order", description = "Cancel a purchase order")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(
            @PathVariable Long id,
            @RequestParam String reason) {
        purchaseOrderService.cancelOrder(id, reason);
        return ResponseEntity.ok(ApiResponse.success("Purchase order cancelled successfully", null));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID", description = "Get purchase order details by ID")
    public ResponseEntity<ApiResponse<PurchaseOrder>> getOrderById(@PathVariable Long id) {
        PurchaseOrder order = purchaseOrderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @GetMapping("/number/{orderNumber}")
    @Operation(summary = "Get order by number", description = "Get purchase order by order number")
    public ResponseEntity<ApiResponse<PurchaseOrder>> getOrderByNumber(@PathVariable String orderNumber) {
        PurchaseOrder order = purchaseOrderService.getOrderByNumber(orderNumber);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @GetMapping("/supplier/{supplierId}")
    @Operation(summary = "Get orders by supplier", description = "Get all orders from a supplier")
    public ResponseEntity<ApiResponse<List<PurchaseOrder>>> getOrdersBySupplier(@PathVariable Long supplierId) {
        List<PurchaseOrder> orders = purchaseOrderService.getOrdersBySupplier(supplierId);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get orders by status", description = "Get orders by status")
    public ResponseEntity<ApiResponse<List<PurchaseOrder>>> getOrdersByStatus(@PathVariable OrderStatus status) {
        List<PurchaseOrder> orders = purchaseOrderService.getOrdersByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/overdue")
    @Operation(summary = "Get overdue orders", description = "Get orders that are overdue for delivery")
    public ResponseEntity<ApiResponse<List<PurchaseOrder>>> getOverdueOrders() {
        List<PurchaseOrder> orders = purchaseOrderService.getOverdueOrders();
        return ResponseEntity.ok(ApiResponse.success(orders));
    }
}