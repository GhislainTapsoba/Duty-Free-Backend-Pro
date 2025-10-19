package com.djbc.dutyfree.service;

import com.djbc.dutyfree.domain.dto.response.ReportResponse;
import com.djbc.dutyfree.domain.entity.Sale;
import com.djbc.dutyfree.domain.entity.SaleItem;
import com.djbc.dutyfree.domain.enums.PaymentMethod;
import com.djbc.dutyfree.repository.PaymentRepository;
import com.djbc.dutyfree.repository.SaleItemRepository;
import com.djbc.dutyfree.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final PaymentRepository paymentRepository;

    @Transactional(readOnly = true)
    public ReportResponse.SalesReport generateSalesReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // Get total revenue
        BigDecimal totalRevenue = saleRepository.getTotalRevenueBetween(startDateTime, endDateTime);
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

        // Get total transactions
        Long totalTransactions = saleRepository.countSalesBetween(startDateTime, endDateTime);

        // Calculate average ticket
        BigDecimal averageTicket = BigDecimal.ZERO;
        if (totalTransactions > 0) {
            averageTicket = totalRevenue.divide(
                    BigDecimal.valueOf(totalTransactions),
                    2,
                    RoundingMode.HALF_UP
            );
        }

        // Revenue by payment method
        Map<String, BigDecimal> revenueByPaymentMethod = new HashMap<>();
        List<Object[]> paymentMethodData = paymentRepository.getRevenueByPaymentMethodBetween(startDateTime, endDateTime);

        for (Object[] data : paymentMethodData) {
            PaymentMethod method = (PaymentMethod) data[0];
            BigDecimal amount = (BigDecimal) data[1];
            revenueByPaymentMethod.put(method.name(), amount);
        }

        // Revenue by category
        Map<String, BigDecimal> revenueByCategory = getRevenueByCategory(startDateTime, endDateTime);

        // Top products
        List<ReportResponse.TopProduct> topProducts = getTopProducts(startDateTime, endDateTime, 10);

        // Daily sales
        List<ReportResponse.DailySales> dailySales = getDailySales(startDate, endDate);

        return ReportResponse.SalesReport.builder()
                .totalRevenue(totalRevenue)
                .totalTransactions(totalTransactions.intValue())
                .averageTicket(averageTicket)
                .revenueByPaymentMethod(revenueByPaymentMethod)
                .revenueByCategory(revenueByCategory)
                .topProducts(topProducts)
                .dailySales(dailySales)
                .build();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> generateDailySalesReport(LocalDate date) {
        LocalDateTime startDateTime = date.atStartOfDay();
        LocalDateTime endDateTime = date.atTime(LocalTime.MAX);

        Map<String, Object> report = new HashMap<>();

        BigDecimal totalRevenue = saleRepository.getTotalRevenueBetween(startDateTime, endDateTime);
        Long totalTransactions = saleRepository.countSalesBetween(startDateTime, endDateTime);
        BigDecimal averageTicket = saleRepository.getAverageTicketBetween(startDateTime, endDateTime);

        report.put("date", date);
        report.put("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
        report.put("totalTransactions", totalTransactions);
        report.put("averageTicket", averageTicket != null ? averageTicket : BigDecimal.ZERO);

        return report;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> generateCashierReport(Long cashierId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<Sale> sales = saleRepository.findByCashierAndDateBetween(cashierId, startDateTime, endDateTime);

        Map<String, Object> report = new HashMap<>();

        BigDecimal totalRevenue = sales.stream()
                .map(Sale::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Integer totalTransactions = sales.size();

        BigDecimal averageTicket = BigDecimal.ZERO;
        if (totalTransactions > 0) {
            averageTicket = totalRevenue.divide(
                    BigDecimal.valueOf(totalTransactions),
                    2,
                    RoundingMode.HALF_UP
            );
        }

        report.put("cashierId", cashierId);
        report.put("startDate", startDate);
        report.put("endDate", endDate);
        report.put("totalRevenue", totalRevenue);
        report.put("totalTransactions", totalTransactions);
        report.put("averageTicket", averageTicket);
        report.put("sales", sales);

        return report;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> generateCashRegisterReport(Long cashRegisterId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<Sale> sales = saleRepository.findByCashRegisterAndDateBetween(cashRegisterId, startDateTime, endDateTime);

        Map<String, Object> report = new HashMap<>();

        BigDecimal totalRevenue = sales.stream()
                .map(Sale::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Integer totalTransactions = sales.size();

        report.put("cashRegisterId", cashRegisterId);
        report.put("startDate", startDate);
        report.put("endDate", endDate);
        report.put("totalRevenue", totalRevenue);
        report.put("totalTransactions", totalTransactions);
        report.put("sales", sales);

        return report;
    }

    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getRevenueByCategory(LocalDateTime startDate, LocalDateTime endDate) {
        List<Sale> sales = saleRepository.findBySaleDateBetween(startDate, endDate, null).getContent();

        Map<String, BigDecimal> revenueByCategory = new HashMap<>();

        for (Sale sale : sales) {
            for (SaleItem item : sale.getItems()) {
                String categoryName = item.getProduct().getCategory().getName();
                BigDecimal revenue = revenueByCategory.getOrDefault(categoryName, BigDecimal.ZERO);
                revenueByCategory.put(categoryName, revenue.add(item.getTotalPrice()));
            }
        }

        return revenueByCategory;
    }

    @Transactional(readOnly = true)
    public List<ReportResponse.TopProduct> getTopProducts(LocalDateTime startDate, LocalDateTime endDate, int limit) {
        List<Sale> sales = saleRepository.findBySaleDateBetween(startDate, endDate, null).getContent();

        Map<String, TopProductData> productDataMap = new HashMap<>();

        for (Sale sale : sales) {
            if (sale.getStatus().name().equals("COMPLETED")) {
                for (SaleItem item : sale.getItems()) {
                    String productName = item.getProduct().getNameFr();
                    TopProductData data = productDataMap.getOrDefault(productName, new TopProductData());
                    data.quantitySold += item.getQuantity();
                    data.revenue = data.revenue.add(item.getTotalPrice());
                    productDataMap.put(productName, data);
                }
            }
        }

        return productDataMap.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().revenue.compareTo(e1.getValue().revenue))
                .limit(limit)
                .map(entry -> ReportResponse.TopProduct.builder()
                        .productName(entry.getKey())
                        .quantitySold(entry.getValue().quantitySold)
                        .revenue(entry.getValue().revenue)
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReportResponse.DailySales> getDailySales(LocalDate startDate, LocalDate endDate) {
        List<ReportResponse.DailySales> dailySalesList = new ArrayList<>();

        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            LocalDateTime startDateTime = currentDate.atStartOfDay();
            LocalDateTime endDateTime = currentDate.atTime(LocalTime.MAX);

            BigDecimal revenue = saleRepository.getTotalRevenueBetween(startDateTime, endDateTime);
            Long transactions = saleRepository.countSalesBetween(startDateTime, endDateTime);

            dailySalesList.add(ReportResponse.DailySales.builder()
                    .date(currentDate)
                    .revenue(revenue != null ? revenue : BigDecimal.ZERO)
                    .transactions(transactions.intValue())
                    .build());

            currentDate = currentDate.plusDays(1);
        }

        return dailySalesList;
    }

    // Helper class
    private static class TopProductData {
        Integer quantitySold = 0;
        BigDecimal revenue = BigDecimal.ZERO;
    }
}