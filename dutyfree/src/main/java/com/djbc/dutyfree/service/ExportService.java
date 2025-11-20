package com.djbc.dutyfree.service;

import com.djbc.dutyfree.domain.dto.response.ReportResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class ExportService {

    private final ReportService reportService;

    public byte[] exportToPdf(LocalDate startDate, LocalDate endDate) {
        ReportResponse.SalesReport report = reportService.generateSalesReport(startDate, endDate);
        
        StringBuilder content = new StringBuilder();
        content.append("RAPPORT DE VENTES\n");
        content.append("================\n\n");
        content.append("Période: ").append(startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
               .append(" - ").append(endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n\n");
        content.append("Chiffre d'affaires total: ").append(report.getTotalRevenue()).append(" XOF\n");
        content.append("Nombre de transactions: ").append(report.getTotalTransactions()).append("\n");
        content.append("Ticket moyen: ").append(report.getAverageTicket()).append(" XOF\n\n");
        
        if (!report.getRevenueByPaymentMethod().isEmpty()) {
            content.append("REVENUS PAR MODE DE PAIEMENT:\n");
            report.getRevenueByPaymentMethod().forEach((method, amount) -> 
                content.append("- ").append(method).append(": ").append(amount).append(" XOF\n"));
            content.append("\n");
        }
        
        return content.toString().getBytes();
    }

    public byte[] exportToExcel(LocalDate startDate, LocalDate endDate) {
        ReportResponse.SalesReport report = reportService.generateSalesReport(startDate, endDate);
        
        StringBuilder csv = new StringBuilder();
        csv.append("Type,Valeur\n");
        csv.append("Chiffre d'affaires total,").append(report.getTotalRevenue()).append("\n");
        csv.append("Nombre de transactions,").append(report.getTotalTransactions()).append("\n");
        csv.append("Ticket moyen,").append(report.getAverageTicket()).append("\n");
        
        return csv.toString().getBytes();
    }
}