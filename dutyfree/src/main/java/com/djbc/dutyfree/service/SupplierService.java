package com.djbc.dutyfree.service;

import com.djbc.dutyfree.domain.entity.Supplier;
import com.djbc.dutyfree.exception.ResourceNotFoundException;
import com.djbc.dutyfree.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupplierService {

    private final SupplierRepository supplierRepository;

    @Transactional(readOnly = true)
    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll().stream()
                .filter(supplier -> !supplier.getDeleted())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Supplier> getActiveSuppliers() {
        return supplierRepository.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public Supplier getSupplierById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", id));
    }

    @Transactional(readOnly = true)
    public Supplier getSupplierByCode(String code) {
        return supplierRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "code", code));
    }

    @Transactional(readOnly = true)
    public List<Supplier> searchSuppliers(String search) {
        return supplierRepository.searchSuppliers(search);
    }

    @Transactional
    public Supplier createSupplier(Supplier supplier) {
        // Check if supplier with same code already exists
        if (supplier.getCode() != null && supplierRepository.existsByCode(supplier.getCode())) {
            throw new IllegalArgumentException("Supplier with code " + supplier.getCode() + " already exists");
        }

        supplier.setDeleted(false);
        if (supplier.getActive() == null) {
            supplier.setActive(true);
        }

        Supplier savedSupplier = supplierRepository.save(supplier);
        log.info("Supplier created: {} - {}", savedSupplier.getCode(), savedSupplier.getName());
        return savedSupplier;
    }

    @Transactional
    public Supplier updateSupplier(Long id, Supplier supplierDetails) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", id));

        // Check if another supplier with the same code exists
        if (supplierDetails.getCode() != null &&
            !supplierDetails.getCode().equals(supplier.getCode()) &&
            supplierRepository.existsByCode(supplierDetails.getCode())) {
            throw new IllegalArgumentException("Supplier with code " + supplierDetails.getCode() + " already exists");
        }

        // Update fields
        if (supplierDetails.getCode() != null) supplier.setCode(supplierDetails.getCode());
        if (supplierDetails.getName() != null) supplier.setName(supplierDetails.getName());
        if (supplierDetails.getContactPerson() != null) supplier.setContactPerson(supplierDetails.getContactPerson());
        if (supplierDetails.getEmail() != null) supplier.setEmail(supplierDetails.getEmail());
        if (supplierDetails.getPhone() != null) supplier.setPhone(supplierDetails.getPhone());
        if (supplierDetails.getAddress() != null) supplier.setAddress(supplierDetails.getAddress());
        if (supplierDetails.getCity() != null) supplier.setCity(supplierDetails.getCity());
        if (supplierDetails.getCountry() != null) supplier.setCountry(supplierDetails.getCountry());
        if (supplierDetails.getPostalCode() != null) supplier.setPostalCode(supplierDetails.getPostalCode());
        if (supplierDetails.getTaxId() != null) supplier.setTaxId(supplierDetails.getTaxId());
        if (supplierDetails.getPaymentTerms() != null) supplier.setPaymentTerms(supplierDetails.getPaymentTerms());
        if (supplierDetails.getCreditLimit() != null) supplier.setCreditLimit(supplierDetails.getCreditLimit());
        if (supplierDetails.getNotes() != null) supplier.setNotes(supplierDetails.getNotes());
        if (supplierDetails.getActive() != null) supplier.setActive(supplierDetails.getActive());

        Supplier updatedSupplier = supplierRepository.save(supplier);
        log.info("Supplier updated: {} - {}", updatedSupplier.getCode(), updatedSupplier.getName());
        return updatedSupplier;
    }

    @Transactional
    public void deleteSupplier(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", id));

        // Soft delete
        supplier.setDeleted(true);
        supplier.setActive(false);
        supplierRepository.save(supplier);
        log.info("Supplier soft deleted: {} - {}", supplier.getCode(), supplier.getName());
    }
}