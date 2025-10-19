package com.djbc.dutyfree.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {
    private String reportType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Map<String, Object> data;

    // Sales Report
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalesReport {
        private BigDecimal totalRevenue;
        private Integer totalTransactions;
        private BigDecimal averageTicket;
        private Map<String, BigDecimal> revenueByPaymentMethod;
        private Map<String, BigDecimal> revenueByCategory;
        private List<TopProduct> topProducts;
        private List<DailySales> dailySales;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopProduct {
        private String productName;
        private Integer quantitySold;
        private BigDecimal revenue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailySales {
        private LocalDate date;
        private BigDecimal revenue;
        private Integer transactions;
    }
}