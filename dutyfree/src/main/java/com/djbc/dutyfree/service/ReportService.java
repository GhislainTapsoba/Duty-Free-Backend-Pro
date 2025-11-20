package com.djbc.dutyfree.service;

import com.djbc.dutyfree.domain.dto.response.ReportResponse;
import com.djbc.dutyfree.domain.entity.Sale;
import com.djbc.dutyfree.domain.entity.SaleItem;
import com.djbc.dutyfree.domain.enums.PaymentMethod;
import com.djbc.dutyfree.repository.CashRegisterRepository;
import com.djbc.dutyfree.repository.PassengerCountRepository;
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
    private final PassengerCountRepository passengerCountRepository;
    private final CashRegisterRepository cashRegisterRepository;

    @Transactional(readOnly = true)
    public ReportResponse.SalesReport generateSalesReport(LocalDate startDate, LocalDate endDate) {
        try {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

            // Get total revenue
            BigDecimal totalRevenue = saleRepository.getTotalRevenueBetween(startDateTime, endDateTime);
            if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

            // Get total transactions
            Long totalTransactions = saleRepository.countSalesBetween(startDateTime, endDateTime);
            if (totalTransactions == null) totalTransactions = 0L;

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
            try {
                List<Object[]> paymentMethodData = paymentRepository.getRevenueByPaymentMethodBetween(startDateTime, endDateTime);
                if (paymentMethodData != null) {
                    for (Object[] data : paymentMethodData) {
                        if (data != null && data.length >= 2 && data[0] != null && data[1] != null) {
                            PaymentMethod method = (PaymentMethod) data[0];
                            BigDecimal amount = (BigDecimal) data[1];
                            revenueByPaymentMethod.put(method.name(), amount);
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Error getting revenue by payment method: {}", e.getMessage());
            }

            // Revenue by category
            Map<String, BigDecimal> revenueByCategory = new HashMap<>();
            try {
                revenueByCategory = getRevenueByCategory(startDateTime, endDateTime);
            } catch (Exception e) {
                log.warn("Error getting revenue by category: {}", e.getMessage());
            }

            // Top products
            List<ReportResponse.TopProduct> topProducts = new ArrayList<>();
            try {
                topProducts = getTopProducts(startDateTime, endDateTime, 10);
            } catch (Exception e) {
                log.warn("Error getting top products: {}", e.getMessage());
            }

            // Daily sales
            List<ReportResponse.DailySales> dailySales = new ArrayList<>();
            try {
                dailySales = getDailySales(startDate, endDate);
            } catch (Exception e) {
                log.warn("Error getting daily sales: {}", e.getMessage());
            }

            return ReportResponse.SalesReport.builder()
                    .totalRevenue(totalRevenue)
                    .totalTransactions(totalTransactions.intValue())
                    .averageTicket(averageTicket)
                    .revenueByPaymentMethod(revenueByPaymentMethod)
                    .revenueByCategory(revenueByCategory)
                    .topProducts(topProducts)
                    .dailySales(dailySales)
                    .build();
        } catch (Exception e) {
            log.error("Error generating sales report: {}", e.getMessage(), e);
            // Return empty report instead of throwing exception
            return ReportResponse.SalesReport.builder()
                    .totalRevenue(BigDecimal.ZERO)
                    .totalTransactions(0)
                    .averageTicket(BigDecimal.ZERO)
                    .revenueByPaymentMethod(new HashMap<>())
                    .revenueByCategory(new HashMap<>())
                    .topProducts(new ArrayList<>())
                    .dailySales(new ArrayList<>())
                    .build();
        }
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
        Map<String, BigDecimal> revenueByCategory = new HashMap<>();
        
        try {
            List<Sale> sales = saleRepository.findBySaleDateBetween(startDate, endDate, null).getContent();
            
            if (sales != null) {
                for (Sale sale : sales) {
                    if (sale != null && sale.getItems() != null) {
                        for (SaleItem item : sale.getItems()) {
                            if (item != null && item.getProduct() != null && 
                                item.getProduct().getCategory() != null && 
                                item.getProduct().getCategory().getName() != null &&
                                item.getTotalPrice() != null) {
                                String categoryName = item.getProduct().getCategory().getName();
                                BigDecimal revenue = revenueByCategory.getOrDefault(categoryName, BigDecimal.ZERO);
                                revenueByCategory.put(categoryName, revenue.add(item.getTotalPrice()));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error calculating revenue by category: {}", e.getMessage());
        }
        
        return revenueByCategory;
    }

    @Transactional(readOnly = true)
    public List<ReportResponse.TopProduct> getTopProducts(LocalDateTime startDate, LocalDateTime endDate, int limit) {
        Map<String, TopProductData> productDataMap = new HashMap<>();
        
        try {
            List<Sale> sales = saleRepository.findBySaleDateBetween(startDate, endDate, null).getContent();
            
            if (sales != null) {
                for (Sale sale : sales) {
                    if (sale != null && sale.getStatus() != null && 
                        "COMPLETED".equals(sale.getStatus().name()) && 
                        sale.getItems() != null) {
                        for (SaleItem item : sale.getItems()) {
                            if (item != null && item.getProduct() != null && 
                                item.getProduct().getNameFr() != null &&
                                item.getQuantity() != null &&
                                item.getTotalPrice() != null) {
                                String productName = item.getProduct().getNameFr();
                                TopProductData data = productDataMap.getOrDefault(productName, new TopProductData());
                                data.quantitySold += item.getQuantity();
                                data.revenue = data.revenue.add(item.getTotalPrice());
                                productDataMap.put(productName, data);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error calculating top products: {}", e.getMessage());
            return new ArrayList<>();
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

        try {
            LocalDate currentDate = startDate;
            while (!currentDate.isAfter(endDate)) {
                LocalDateTime startDateTime = currentDate.atStartOfDay();
                LocalDateTime endDateTime = currentDate.atTime(LocalTime.MAX);

                BigDecimal revenue = BigDecimal.ZERO;
                Integer transactions = 0;
                
                try {
                    BigDecimal revenueResult = saleRepository.getTotalRevenueBetween(startDateTime, endDateTime);
                    revenue = revenueResult != null ? revenueResult : BigDecimal.ZERO;
                    
                    Long transactionsResult = saleRepository.countSalesBetween(startDateTime, endDateTime);
                    transactions = transactionsResult != null ? transactionsResult.intValue() : 0;
                } catch (Exception e) {
                    log.warn("Error getting daily sales for date {}: {}", currentDate, e.getMessage());
                }

                dailySalesList.add(ReportResponse.DailySales.builder()
                        .date(currentDate)
                        .revenue(revenue)
                        .transactions(transactions)
                        .build());

                currentDate = currentDate.plusDays(1);
            }
        } catch (Exception e) {
            log.error("Error generating daily sales: {}", e.getMessage());
        }

        return dailySalesList;
    }

    /**
     * Generate capture rate report (taux de capture = tickets / passagers)
     */
    @Transactional(readOnly = true)
    public Map<String, Object> generateCaptureRateReport(LocalDate startDate, LocalDate endDate) {
        log.info("Generating capture rate report from {} to {}", startDate, endDate);

        Map<String, Object> report = new HashMap<>();

        try {
            // Get total sales (tickets)
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
            Long totalSales = saleRepository.countSalesBetween(startDateTime, endDateTime);
            if (totalSales == null) totalSales = 0L;

            // Get total passengers
            Integer totalPassengers = passengerCountRepository.getTotalPassengersBetween(startDate, endDate);
            if (totalPassengers == null) totalPassengers = 0;

            // Calculate capture rate
            BigDecimal captureRate = BigDecimal.ZERO;
            if (totalPassengers > 0) {
                captureRate = BigDecimal.valueOf(totalSales)
                        .divide(BigDecimal.valueOf(totalPassengers), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)); // Convert to percentage
            }

            // Get revenue per passenger
            BigDecimal totalRevenue = saleRepository.getTotalRevenueBetween(startDateTime, endDateTime);
            if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

            BigDecimal revenuePerPassenger = BigDecimal.ZERO;
            if (totalPassengers > 0) {
                revenuePerPassenger = totalRevenue.divide(
                        BigDecimal.valueOf(totalPassengers),
                        2,
                        RoundingMode.HALF_UP
                );
            }

            report.put("startDate", startDate);
            report.put("endDate", endDate);
            report.put("totalSales", totalSales);
            report.put("totalPassengers", totalPassengers);
            report.put("captureRate", captureRate); // Percentage
            report.put("totalRevenue", totalRevenue);
            report.put("revenuePerPassenger", revenuePerPassenger);
            report.put("averageTicket", totalSales > 0 ? totalRevenue.divide(BigDecimal.valueOf(totalSales), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO);

        } catch (Exception e) {
            log.error("Error generating capture rate report: {}", e.getMessage(), e);
            report.put("error", e.getMessage());
        }

        return report;
    }

    /**
     * Generate sales by POS (point of sale / cash register) report
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> generateSalesByPOSReport(LocalDate startDate, LocalDate endDate) {
        log.info("Generating sales by POS report from {} to {}", startDate, endDate);

        List<Map<String, Object>> posList = new ArrayList<>();

        try {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

            // Get all cash registers
            var cashRegisters = cashRegisterRepository.findAll();

            for (var cashRegister : cashRegisters) {
                Map<String, Object> posData = new HashMap<>();

                // Get sales for this POS
                List<Sale> sales = saleRepository.findByCashRegisterAndDateBetween(
                        cashRegister.getId(),
                        startDateTime,
                        endDateTime
                );

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

                posData.put("cashRegisterId", cashRegister.getId());
                posData.put("registerNumber", cashRegister.getRegisterNumber());
                posData.put("registerName", cashRegister.getName());
                posData.put("location", cashRegister.getLocation());
                posData.put("totalRevenue", totalRevenue);
                posData.put("totalTransactions", totalTransactions);
                posData.put("averageTicket", averageTicket);
                posData.put("active", cashRegister.getActive());

                posList.add(posData);
            }

            // Sort by revenue descending
            posList.sort((a, b) -> {
                BigDecimal revenueA = (BigDecimal) a.get("totalRevenue");
                BigDecimal revenueB = (BigDecimal) b.get("totalRevenue");
                return revenueB.compareTo(revenueA);
            });

        } catch (Exception e) {
            log.error("Error generating sales by POS report: {}", e.getMessage(), e);
        }

        return posList;
    }

    /**
     * Generate average ticket report with details
     */
    @Transactional(readOnly = true)
    public Map<String, Object> generateAverageTicketReport(LocalDate startDate, LocalDate endDate) {
        log.info("Generating average ticket report from {} to {}", startDate, endDate);

        Map<String, Object> report = new HashMap<>();

        try {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

            BigDecimal totalRevenue = saleRepository.getTotalRevenueBetween(startDateTime, endDateTime);
            if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

            Long totalTransactions = saleRepository.countSalesBetween(startDateTime, endDateTime);
            if (totalTransactions == null) totalTransactions = 0L;

            BigDecimal averageTicket = BigDecimal.ZERO;
            if (totalTransactions > 0) {
                averageTicket = totalRevenue.divide(
                        BigDecimal.valueOf(totalTransactions),
                        2,
                        RoundingMode.HALF_UP
                );
            }

            // Get min and max tickets
            List<Sale> sales = saleRepository.findBySaleDateBetween(startDateTime, endDateTime, null).getContent();

            BigDecimal minTicket = sales.stream()
                    .map(Sale::getTotalAmount)
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);

            BigDecimal maxTicket = sales.stream()
                    .map(Sale::getTotalAmount)
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);

            report.put("startDate", startDate);
            report.put("endDate", endDate);
            report.put("totalRevenue", totalRevenue);
            report.put("totalTransactions", totalTransactions);
            report.put("averageTicket", averageTicket);
            report.put("minTicket", minTicket);
            report.put("maxTicket", maxTicket);

        } catch (Exception e) {
            log.error("Error generating average ticket report: {}", e.getMessage(), e);
            report.put("error", e.getMessage());
        }

        return report;
    }

    // Helper class
    private static class TopProductData {
        Integer quantitySold = 0;
        BigDecimal revenue = BigDecimal.ZERO;
    }
}