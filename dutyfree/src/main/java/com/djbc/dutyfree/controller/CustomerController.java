package com.djbc.dutyfree.controller;

import com.djbc.dutyfree.domain.dto.response.ApiResponse;
import com.djbc.dutyfree.domain.entity.Customer;
import com.djbc.dutyfree.service.CustomerService;
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
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Customers", description = "Customer management APIs")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @Operation(summary = "Create customer", description = "Create a new customer")
    public ResponseEntity<ApiResponse<Customer>> createCustomer(@Valid @RequestBody Customer customer) {
        Customer createdCustomer = customerService.createCustomer(customer);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Customer created successfully", createdCustomer));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update customer", description = "Update existing customer")
    public ResponseEntity<ApiResponse<Customer>> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody Customer customer) {
        Customer updatedCustomer = customerService.updateCustomer(id, customer);
        return ResponseEntity.ok(ApiResponse.success("Customer updated successfully", updatedCustomer));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID", description = "Get customer details by ID")
    public ResponseEntity<ApiResponse<Customer>> getCustomerById(@PathVariable Long id) {
        Customer customer = customerService.getCustomerById(id);
        return ResponseEntity.ok(ApiResponse.success(customer));
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Get customer by email", description = "Get customer details by email")
    public ResponseEntity<ApiResponse<Customer>> getCustomerByEmail(@PathVariable String email) {
        Customer customer = customerService.getCustomerByEmail(email);
        return ResponseEntity.ok(ApiResponse.success(customer));
    }

    @GetMapping("/phone/{phone}")
    @Operation(summary = "Get customer by phone", description = "Get customer details by phone")
    public ResponseEntity<ApiResponse<Customer>> getCustomerByPhone(@PathVariable String phone) {
        Customer customer = customerService.getCustomerByPhone(phone);
        return ResponseEntity.ok(ApiResponse.success(customer));
    }

    @GetMapping
    @Operation(summary = "Get all customers", description = "Get all active customers")
    public ResponseEntity<ApiResponse<List<Customer>>> getAllCustomers() {
        List<Customer> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(ApiResponse.success(customers));
    }

    @GetMapping("/search")
    @Operation(summary = "Search customers", description = "Search customers by name, email or phone")
    public ResponseEntity<ApiResponse<List<Customer>>> searchCustomers(@RequestParam String query) {
        List<Customer> customers = customerService.searchCustomers(query);
        return ResponseEntity.ok(ApiResponse.success(customers));
    }

    @GetMapping("/vip")
    @Operation(summary = "Get VIP customers", description = "Get all VIP customers")
    public ResponseEntity<ApiResponse<List<Customer>>> getVIPCustomers() {
        List<Customer> customers = customerService.getVIPCustomers();
        return ResponseEntity.ok(ApiResponse.success(customers));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete customer", description = "Soft delete a customer")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.ok(ApiResponse.success("Customer deleted successfully", null));
    }
}