package com.djbc.dutyfree.util;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ReportUtil {
    
    /**
     * Generate Excel report
     */
    public byte[] generateExcelReport(String reportTitle, List<String> headers, List<List<Object>> data) 
            throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); 
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet(reportTitle);
            
            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            // Create title row
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(reportTitle);
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleStyle.setFont(titleFont);
            titleCell.setCellStyle(titleStyle);
            
            // Create header row
            Row headerRow = sheet.createRow(2);
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
            }
            
            // Create data rows
            int rowNum = 3;
            for (List<Object> rowData : data) {
                Row row = sheet.createRow(rowNum++);
                for (int i = 0; i < rowData.size(); i++) {
                    Cell cell = row.createCell(i);
                    Object value = rowData.get(i);
                    
                    if (value instanceof String) {
                        cell.setCellValue((String) value);
                    } else if (value instanceof Number) {
                        cell.setCellValue(((Number) value).doubleValue());
                    } else if (value instanceof LocalDate) {
                        cell.setCellValue(value.toString());
                    } else if (value != null) {
                        cell.setCellValue(value.toString());
                    }
                }
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(out);
            return out.toByteArray();
        }
    }
    
    /**
     * Generate PDF report
     */
    public byte[] generatePdfReport(String reportTitle, List<String> headers, List<List<Object>> data) 
            throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            
            // Add title
            document.add(new Paragraph(reportTitle)
                    .setFontSize(18)
                    .setBold());
            
            // Add date
            document.add(new Paragraph("Generated on: " + DateUtil.formatDate(LocalDate.now()))
                    .setFontSize(10));
            
            document.add(new Paragraph("\n"));
            
            // Create table
            Table table = new Table(headers.size());
            
            // Add headers
            for (String header : headers) {
                table.addHeaderCell(header);
            }
            
            // Add data
            for (List<Object> rowData : data) {
                for (Object value : rowData) {
                    if (value != null) {
                        table.addCell(value.toString());
                    } else {
                        table.addCell("");
                    }
                }
            }
            
            document.add(table);
            document.close();
            
            return out.toByteArray();
        }
    }
    
    /**
     * Generate sales summary
     */
    public Map<String, Object> generateSalesSummary(
            BigDecimal totalRevenue,
            Long totalTransactions,
            BigDecimal averageTicket) {
        
        return Map.of(
            "totalRevenue", CurrencyUtil.formatXOF(totalRevenue),
            "totalTransactions", totalTransactions,
            "averageTicket", CurrencyUtil.formatXOF(averageTicket),
            "generatedAt", DateUtil.getCurrentDateTime()
        );
    }
    
    /**
     * Calculate percentage change
     */
    public BigDecimal calculatePercentageChange(BigDecimal oldValue, BigDecimal newValue) {
        if (oldValue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal change = newValue.subtract(oldValue);
        return change.divide(oldValue, 2, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
}