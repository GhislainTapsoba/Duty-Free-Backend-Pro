package com.djbc.dutyfree.service;

import com.djbc.dutyfree.domain.dto.request.CreateWastageRequest;
import com.djbc.dutyfree.domain.dto.response.WastageResponse;
import com.djbc.dutyfree.domain.entity.Product;
import com.djbc.dutyfree.domain.entity.Wastage;
import com.djbc.dutyfree.exception.ResourceNotFoundException;
import com.djbc.dutyfree.repository.ProductRepository;
import com.djbc.dutyfree.repository.WastageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WastageService {

    private final WastageRepository wastageRepository;
    private final ProductRepository productRepository;

    @Transactional
    public WastageResponse create(CreateWastageRequest request, String username) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

        // Calculer la valeur perdue
        BigDecimal valueLost = product.getPurchasePrice()
                .multiply(BigDecimal.valueOf(request.getQuantity()));

        Wastage wastage = Wastage.builder()
                .product(product)
                .quantity(request.getQuantity())
                .wastageDate(request.getWastageDate())
                .reason(request.getReason())
                .description(request.getDescription())
                .reportedBy(username)
                .valueLost(valueLost)
                .approved(false)
                .build();

        wastage = wastageRepository.save(wastage);
        log.info("Wastage created: {} units of product {}", request.getQuantity(), product.getNameFr());

        return mapToResponse(wastage);
    }

    @Transactional(readOnly = true)
    public List<WastageResponse> getAll() {
        return wastageRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WastageResponse> getByDateRange(LocalDate startDate, LocalDate endDate) {
        return wastageRepository.findByDateRange(startDate, endDate).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WastageResponse> getPending() {
        return wastageRepository.findByApproved(false).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public WastageResponse approve(Long id, String username) {
        Wastage wastage = wastageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Wastage", "id", id));

        wastage.setApproved(true);
        wastage.setApprovedBy(username);
        wastage.setApprovalDate(LocalDate.now());

        wastage = wastageRepository.save(wastage);
        log.info("Wastage approved: {} by {}", id, username);

        return mapToResponse(wastage);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalValueLost(LocalDate startDate, LocalDate endDate) {
        BigDecimal total = wastageRepository.getTotalValueLost(startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }

    private WastageResponse mapToResponse(Wastage wastage) {
        return WastageResponse.builder()
                .id(wastage.getId())
                .productId(wastage.getProduct().getId())
                .productName(wastage.getProduct().getNameFr())
                .productSku(wastage.getProduct().getSku())
                .quantity(wastage.getQuantity())
                .wastageDate(wastage.getWastageDate())
                .reason(wastage.getReason())
                .description(wastage.getDescription())
                .reportedBy(wastage.getReportedBy())
                .valueLost(wastage.getValueLost())
                .approved(wastage.getApproved())
                .approvedBy(wastage.getApprovedBy())
                .approvalDate(wastage.getApprovalDate())
                .build();
    }
}