package com.djbc.dutyfree.service;

import com.djbc.dutyfree.domain.entity.Sale;
import com.djbc.dutyfree.domain.entity.SaleItem;
import com.djbc.dutyfree.domain.entity.Settings;
import com.djbc.dutyfree.domain.entity.Sommier;
import com.djbc.dutyfree.domain.enums.SaleStatus;
import com.djbc.dutyfree.repository.SaleRepository;
import com.djbc.dutyfree.repository.SettingsRepository;
import com.djbc.dutyfree.repository.SommierRepository;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.io.font.constants.StandardFonts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for generating customs compliance reports
 * Handles sommier apurement (customs clearance tracking) and monthly customs registries
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomsReportService {

    private final SaleRepository saleRepository;
    private final SommierRepository sommierRepository;
    private final SettingsRepository settingsRepository;

    @Value("${app.reports.storage-path:./data/reports}")
    private String reportsStoragePath;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH);
    private static final DateTimeFormatter FILE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final DeviceRgb HEADER_COLOR = new DeviceRgb(41, 128, 185);
    private static final DeviceRgb BORDER_COLOR = new DeviceRgb(189, 195, 199);
    private static final DeviceRgb HIGHLIGHT_COLOR = new DeviceRgb(236, 240, 241);

    /**
     * Generate sommier apurement report (customs clearance tracking)
     * This report tracks all duty-free sales for customs compliance
     *
     * @param startDate Start date of the report period
     * @param endDate End date of the report period
     * @return Path to the generated PDF report
     */
    @Transactional(readOnly = true)
    public String generateSommierApurementReport(LocalDate startDate, LocalDate endDate) throws Exception {
        log.info("Generating sommier apurement report from {} to {}", startDate, endDate);

        // Fetch all completed sales in the period
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        List<Sale> sales = saleRepository.findBySaleDateBetweenAndStatus(
                startDateTime,
                endDateTime,
                SaleStatus.COMPLETED
        );

        // Fetch sommier records
        List<Sommier> sommierRecords = sommierRepository.findByDeclarationDateBetween(
                startDateTime,
                endDateTime
        );

        // Fetch company settings
        Map<String, String> companySettings = fetchCompanySettings();

        // Generate PDF
        String fileName = String.format("sommier_apurement_%s_%s.pdf",
                startDate.format(FILE_DATE_FORMATTER),
                endDate.format(FILE_DATE_FORMATTER));

        String outputPath = Paths.get(reportsStoragePath, fileName).toString();

        generateSommierPDF(sales, sommierRecords, companySettings, startDate, endDate, outputPath);

        log.info("Sommier apurement report generated successfully at: {}", outputPath);
        return outputPath;
    }

    /**
     * Generate monthly customs registry report
     * Aggregated report of all duty-free sales for the month
     *
     * @param yearMonth Year and month for the report
     * @return Path to the generated PDF report
     */
    @Transactional(readOnly = true)
    public String generateMonthlyCustomsRegistry(YearMonth yearMonth) throws Exception {
        log.info("Generating monthly customs registry for {}", yearMonth);

        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        List<Sale> sales = saleRepository.findBySaleDateBetweenAndStatus(
                startDateTime,
                endDateTime,
                SaleStatus.COMPLETED
        );

        Map<String, String> companySettings = fetchCompanySettings();

        String fileName = String.format("registre_douanier_%s.pdf",
                yearMonth.format(DateTimeFormatter.ofPattern("yyyy_MM")));

        String outputPath = Paths.get(reportsStoragePath, fileName).toString();

        generateMonthlyRegistryPDF(sales, companySettings, yearMonth, outputPath);

        log.info("Monthly customs registry generated successfully at: {}", outputPath);
        return outputPath;
    }

    /**
     * Generate daily sales summary for customs
     *
     * @param date Date for the daily report
     * @return Path to the generated PDF report
     */
    @Transactional(readOnly = true)
    public String generateDailySalesSummary(LocalDate date) throws Exception {
        log.info("Generating daily sales summary for {}", date);

        LocalDateTime startDateTime = date.atStartOfDay();
        LocalDateTime endDateTime = date.atTime(23, 59, 59);

        List<Sale> sales = saleRepository.findBySaleDateBetweenAndStatus(
                startDateTime,
                endDateTime,
                SaleStatus.COMPLETED
        );

        Map<String, String> companySettings = fetchCompanySettings();

        String fileName = String.format("recapitulatif_ventes_%s.pdf",
                date.format(FILE_DATE_FORMATTER));

        String outputPath = Paths.get(reportsStoragePath, fileName).toString();

        generateDailySummaryPDF(sales, companySettings, date, outputPath);

        log.info("Daily sales summary generated successfully at: {}", outputPath);
        return outputPath;
    }

    private void generateSommierPDF(List<Sale> sales, List<Sommier> sommierRecords,
                                   Map<String, String> settings, LocalDate startDate,
                                   LocalDate endDate, String outputPath) throws Exception {

        // Ensure output directory exists
        Path path = Paths.get(outputPath);
        Files.createDirectories(path.getParent());

        // Create PDF document
        PdfWriter writer = new PdfWriter(new FileOutputStream(outputPath));
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc, PageSize.A4.rotate()); // Landscape for wide table
        document.setMargins(20, 20, 20, 20);

        PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        // Header
        addReportHeader(document, boldFont, regularFont, settings,
                "RAPPORT D'APUREMENT DU SOMMIER", startDate, endDate);

        // Summary statistics
        addSommierStatistics(document, boldFont, regularFont, sales, sommierRecords);

        // Detailed sales table
        addSommierDetailsTable(document, boldFont, regularFont, sales);

        // Footer with signatures
        addReportFooter(document, regularFont, settings);

        document.close();
    }

    private void generateMonthlyRegistryPDF(List<Sale> sales, Map<String, String> settings,
                                           YearMonth yearMonth, String outputPath) throws Exception {

        Path path = Paths.get(outputPath);
        Files.createDirectories(path.getParent());

        PdfWriter writer = new PdfWriter(new FileOutputStream(outputPath));
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc, PageSize.A4);
        document.setMargins(20, 20, 20, 20);

        PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        // Header
        Paragraph title = new Paragraph("REGISTRE DOUANIER MENSUEL / MONTHLY CUSTOMS REGISTRY")
                .setFont(boldFont)
                .setFontSize(16)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(HEADER_COLOR)
                .setMarginBottom(10);
        document.add(title);

        Paragraph period = new Paragraph("Période / Period: " + yearMonth.format(MONTH_FORMATTER))
                .setFont(boldFont)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(period);

        // Monthly summary
        addMonthlySummary(document, boldFont, regularFont, sales);

        // Sales by category
        addSalesByCategory(document, boldFont, regularFont, sales);

        // Daily breakdown
        addDailyBreakdown(document, boldFont, regularFont, sales, yearMonth);

        // Footer
        addReportFooter(document, regularFont, settings);

        document.close();
    }

    private void generateDailySummaryPDF(List<Sale> sales, Map<String, String> settings,
                                        LocalDate date, String outputPath) throws Exception {

        Path path = Paths.get(outputPath);
        Files.createDirectories(path.getParent());

        PdfWriter writer = new PdfWriter(new FileOutputStream(outputPath));
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc, PageSize.A4);
        document.setMargins(20, 20, 20, 20);

        PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        // Header
        Paragraph title = new Paragraph("RÉCAPITULATIF JOURNALIER DES VENTES / DAILY SALES SUMMARY")
                .setFont(boldFont)
                .setFontSize(14)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(HEADER_COLOR)
                .setMarginBottom(10);
        document.add(title);

        Paragraph dateP = new Paragraph("Date: " + date.format(DATE_FORMATTER))
                .setFont(boldFont)
                .setFontSize(11)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(dateP);

        // Daily statistics
        addDailyStatistics(document, boldFont, regularFont, sales);

        // Sales list
        addDailySalesTable(document, boldFont, regularFont, sales);

        // Footer
        addReportFooter(document, regularFont, settings);

        document.close();
    }

    private void addReportHeader(Document document, PdfFont boldFont, PdfFont regularFont,
                                Map<String, String> settings, String reportTitle,
                                LocalDate startDate, LocalDate endDate) {

        // Company name
        Paragraph companyName = new Paragraph(settings.getOrDefault("companyName", "DJBC DUTY FREE"))
                .setFont(boldFont)
                .setFontSize(16)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(HEADER_COLOR)
                .setMarginBottom(5);
        document.add(companyName);

        // Report title
        Paragraph title = new Paragraph(reportTitle)
                .setFont(boldFont)
                .setFontSize(14)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5);
        document.add(title);

        // Period
        Paragraph period = new Paragraph(String.format("Période: %s au %s",
                startDate.format(DATE_FORMATTER),
                endDate.format(DATE_FORMATTER)))
                .setFont(regularFont)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5);
        document.add(period);

        // Fiscal info
        String ifu = settings.getOrDefault("ifu", "N/A");
        String customsNumber = settings.getOrDefault("customsNumber", "N/A");
        Paragraph fiscalInfo = new Paragraph(String.format("IFU: %s | N° Agrément Douane: %s", ifu, customsNumber))
                .setFont(regularFont)
                .setFontSize(9)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(15);
        document.add(fiscalInfo);

        // Separator
        document.add(new Paragraph()
                .setBorder(new SolidBorder(BORDER_COLOR, 1))
                .setMarginBottom(15));
    }

    private void addSommierStatistics(Document document, PdfFont boldFont, PdfFont regularFont,
                                     List<Sale> sales, List<Sommier> sommierRecords) {

        Paragraph sectionTitle = new Paragraph("STATISTIQUES / STATISTICS")
                .setFont(boldFont)
                .setFontSize(11)
                .setMarginBottom(10);
        document.add(sectionTitle);

        Table statsTable = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1, 1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(15);

        // Calculate statistics
        int totalSales = sales.size();
        BigDecimal totalAmount = sales.stream()
                .map(Sale::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int totalItems = sales.stream()
                .mapToInt(s -> s.getItems().size())
                .sum();
        int sommierCount = sommierRecords.size();

        statsTable.addCell(createStatsCell("Nombre de ventes / Sales count", String.valueOf(totalSales),
                boldFont, regularFont));
        statsTable.addCell(createStatsCell("Montant total / Total amount",
                formatAmount(totalAmount), boldFont, regularFont));
        statsTable.addCell(createStatsCell("Articles vendus / Items sold", String.valueOf(totalItems),
                boldFont, regularFont));
        statsTable.addCell(createStatsCell("Déclarations douanières / Customs declarations",
                String.valueOf(sommierCount), boldFont, regularFont));

        document.add(statsTable);
    }

    private void addSommierDetailsTable(Document document, PdfFont boldFont, PdfFont regularFont,
                                       List<Sale> sales) {

        Paragraph sectionTitle = new Paragraph("DÉTAIL DES VENTES / SALES DETAILS")
                .setFont(boldFont)
                .setFontSize(11)
                .setMarginTop(10)
                .setMarginBottom(10);
        document.add(sectionTitle);

        Table table = new Table(UnitValue.createPercentArray(new float[]{1.5f, 2, 2, 2, 1.5f, 1.5f}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(15);

        // Header
        String[] headers = {"Date", "N° Vente", "Passager", "Destination", "Articles", "Montant TTC"};
        for (String header : headers) {
            table.addHeaderCell(createHeaderCell(header, boldFont));
        }

        // Data rows
        for (Sale sale : sales) {
            table.addCell(createDataCell(sale.getSaleDate().format(DATE_FORMATTER), regularFont));
            table.addCell(createDataCell(sale.getSaleNumber(), regularFont));
            table.addCell(createDataCell(sale.getPassengerName() != null ? sale.getPassengerName() : "N/A", regularFont));
            table.addCell(createDataCell(sale.getDestination() != null ? sale.getDestination() : "N/A", regularFont));
            table.addCell(createDataCell(String.valueOf(sale.getItems().size()), regularFont));
            table.addCell(createDataCell(formatAmount(sale.getTotalAmount()), regularFont));
        }

        document.add(table);
    }

    private void addMonthlySummary(Document document, PdfFont boldFont, PdfFont regularFont,
                                  List<Sale> sales) {

        Paragraph sectionTitle = new Paragraph("RÉSUMÉ MENSUEL / MONTHLY SUMMARY")
                .setFont(boldFont)
                .setFontSize(11)
                .setMarginBottom(10);
        document.add(sectionTitle);

        BigDecimal totalAmount = sales.stream()
                .map(Sale::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalTax = sales.stream()
                .map(Sale::getTaxAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{2, 1}))
                .setWidth(UnitValue.createPercentValue(60))
                .setMarginBottom(20);

        summaryTable.addCell(createDataCell("Nombre total de ventes / Total sales", regularFont));
        summaryTable.addCell(createDataCell(String.valueOf(sales.size()), regularFont)
                .setTextAlignment(TextAlignment.RIGHT));

        summaryTable.addCell(createDataCell("Montant HT / Amount excl. tax", regularFont));
        summaryTable.addCell(createDataCell(formatAmount(totalAmount.subtract(totalTax)), regularFont)
                .setTextAlignment(TextAlignment.RIGHT));

        summaryTable.addCell(createDataCell("TVA / Tax", regularFont));
        summaryTable.addCell(createDataCell(formatAmount(totalTax), regularFont)
                .setTextAlignment(TextAlignment.RIGHT));

        summaryTable.addCell(createDataCell("Montant TTC / Total incl. tax", boldFont)
                .setBackgroundColor(HIGHLIGHT_COLOR));
        summaryTable.addCell(createDataCell(formatAmount(totalAmount), boldFont)
                .setTextAlignment(TextAlignment.RIGHT)
                .setBackgroundColor(HIGHLIGHT_COLOR));

        document.add(summaryTable);
    }

    private void addSalesByCategory(Document document, PdfFont boldFont, PdfFont regularFont,
                                   List<Sale> sales) {

        Paragraph sectionTitle = new Paragraph("VENTES PAR CATÉGORIE / SALES BY CATEGORY")
                .setFont(boldFont)
                .setFontSize(11)
                .setMarginTop(10)
                .setMarginBottom(10);
        document.add(sectionTitle);

        // Group sales by product category
        Map<String, BigDecimal> salesByCategory = new HashMap<>();
        Map<String, Integer> quantityByCategory = new HashMap<>();

        for (Sale sale : sales) {
            for (SaleItem item : sale.getItems()) {
                String categoryName = item.getProduct().getCategory().getName();
                salesByCategory.merge(categoryName, item.getTotalPrice(), BigDecimal::add);
                quantityByCategory.merge(categoryName, item.getQuantity(), Integer::sum);
            }
        }

        Table table = new Table(UnitValue.createPercentArray(new float[]{2, 1, 2}))
                .setWidth(UnitValue.createPercentValue(80))
                .setMarginBottom(20);

        table.addHeaderCell(createHeaderCell("Catégorie", boldFont));
        table.addHeaderCell(createHeaderCell("Quantité", boldFont));
        table.addHeaderCell(createHeaderCell("Montant", boldFont));

        for (Map.Entry<String, BigDecimal> entry : salesByCategory.entrySet()) {
            table.addCell(createDataCell(entry.getKey(), regularFont));
            table.addCell(createDataCell(String.valueOf(quantityByCategory.get(entry.getKey())), regularFont)
                    .setTextAlignment(TextAlignment.CENTER));
            table.addCell(createDataCell(formatAmount(entry.getValue()), regularFont)
                    .setTextAlignment(TextAlignment.RIGHT));
        }

        document.add(table);
    }

    private void addDailyBreakdown(Document document, PdfFont boldFont, PdfFont regularFont,
                                  List<Sale> sales, YearMonth yearMonth) {

        Paragraph sectionTitle = new Paragraph("RÉCAPITULATIF JOURNALIER / DAILY BREAKDOWN")
                .setFont(boldFont)
                .setFontSize(11)
                .setMarginTop(10)
                .setMarginBottom(10);
        document.add(sectionTitle);

        // Group sales by day
        Map<LocalDate, List<Sale>> salesByDay = sales.stream()
                .collect(Collectors.groupingBy(s -> s.getSaleDate().toLocalDate()));

        Table table = new Table(UnitValue.createPercentArray(new float[]{2, 1, 2}))
                .setWidth(UnitValue.createPercentValue(70))
                .setMarginBottom(20);

        table.addHeaderCell(createHeaderCell("Date", boldFont));
        table.addHeaderCell(createHeaderCell("Ventes", boldFont));
        table.addHeaderCell(createHeaderCell("Montant", boldFont));

        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            LocalDate date = yearMonth.atDay(day);
            List<Sale> daySales = salesByDay.getOrDefault(date, Collections.emptyList());

            BigDecimal dayAmount = daySales.stream()
                    .map(Sale::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            table.addCell(createDataCell(date.format(DATE_FORMATTER), regularFont));
            table.addCell(createDataCell(String.valueOf(daySales.size()), regularFont)
                    .setTextAlignment(TextAlignment.CENTER));
            table.addCell(createDataCell(formatAmount(dayAmount), regularFont)
                    .setTextAlignment(TextAlignment.RIGHT));
        }

        document.add(table);
    }

    private void addDailyStatistics(Document document, PdfFont boldFont, PdfFont regularFont,
                                   List<Sale> sales) {

        Table statsTable = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(20);

        BigDecimal totalAmount = sales.stream()
                .map(Sale::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalItems = sales.stream()
                .mapToInt(s -> s.getItems().size())
                .sum();

        statsTable.addCell(createStatsCell("Nombre de ventes", String.valueOf(sales.size()),
                boldFont, regularFont));
        statsTable.addCell(createStatsCell("Articles vendus", String.valueOf(totalItems),
                boldFont, regularFont));
        statsTable.addCell(createStatsCell("Montant total", formatAmount(totalAmount),
                boldFont, regularFont));

        document.add(statsTable);
    }

    private void addDailySalesTable(Document document, PdfFont boldFont, PdfFont regularFont,
                                   List<Sale> sales) {

        Paragraph sectionTitle = new Paragraph("LISTE DES VENTES / SALES LIST")
                .setFont(boldFont)
                .setFontSize(11)
                .setMarginTop(10)
                .setMarginBottom(10);
        document.add(sectionTitle);

        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 2, 2, 1, 2}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(20);

        table.addHeaderCell(createHeaderCell("Heure", boldFont));
        table.addHeaderCell(createHeaderCell("N° Vente", boldFont));
        table.addHeaderCell(createHeaderCell("Caissier", boldFont));
        table.addHeaderCell(createHeaderCell("Articles", boldFont));
        table.addHeaderCell(createHeaderCell("Montant", boldFont));

        for (Sale sale : sales) {
            table.addCell(createDataCell(sale.getSaleDate().format(DateTimeFormatter.ofPattern("HH:mm")), regularFont));
            table.addCell(createDataCell(sale.getSaleNumber(), regularFont));
            table.addCell(createDataCell(sale.getCashier().getFullName(), regularFont));
            table.addCell(createDataCell(String.valueOf(sale.getItems().size()), regularFont)
                    .setTextAlignment(TextAlignment.CENTER));
            table.addCell(createDataCell(formatAmount(sale.getTotalAmount()), regularFont)
                    .setTextAlignment(TextAlignment.RIGHT));
        }

        document.add(table);
    }

    private void addReportFooter(Document document, PdfFont regularFont, Map<String, String> settings) {
        document.add(new Paragraph()
                .setBorder(new SolidBorder(BORDER_COLOR, 1))
                .setMarginTop(20)
                .setMarginBottom(15));

        Table signaturesTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(10);

        signaturesTable.addCell(createSignatureCell("Préparé par / Prepared by:\n\n\n\n_____________\nDate:", regularFont));
        signaturesTable.addCell(createSignatureCell("Approuvé par / Approved by:\n\n\n\n_____________\nDate:", regularFont));

        document.add(signaturesTable);

        Paragraph timestamp = new Paragraph("Généré le / Generated on: " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                .setFont(regularFont)
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(10);
        document.add(timestamp);
    }

    private Cell createHeaderCell(String content, PdfFont font) {
        return new Cell()
                .add(new Paragraph(content).setFont(font).setFontSize(9))
                .setTextAlignment(TextAlignment.CENTER)
                .setBackgroundColor(HEADER_COLOR)
                .setFontColor(new DeviceRgb(255, 255, 255))
                .setBorder(new SolidBorder(BORDER_COLOR, 1))
                .setPadding(5);
    }

    private Cell createDataCell(String content, PdfFont font) {
        return new Cell()
                .add(new Paragraph(content).setFont(font).setFontSize(8))
                .setBorder(new SolidBorder(BORDER_COLOR, 0.5f))
                .setPadding(4);
    }

    private Cell createStatsCell(String label, String value, PdfFont boldFont, PdfFont regularFont) {
        Cell cell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setBackgroundColor(HIGHLIGHT_COLOR)
                .setPadding(10);

        Paragraph labelP = new Paragraph(label)
                .setFont(regularFont)
                .setFontSize(9)
                .setMarginBottom(3);
        Paragraph valueP = new Paragraph(value)
                .setFont(boldFont)
                .setFontSize(13)
                .setFontColor(HEADER_COLOR);

        cell.add(labelP);
        cell.add(valueP);

        return cell;
    }

    private Cell createSignatureCell(String content, PdfFont font) {
        return new Cell()
                .add(new Paragraph(content).setFont(font).setFontSize(9))
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(10);
    }

    private String formatAmount(BigDecimal amount) {
        if (amount == null) {
            return "0.00 FCFA";
        }
        return amount.setScale(2, RoundingMode.HALF_UP).toString() + " FCFA";
    }

    private Map<String, String> fetchCompanySettings() {
        Map<String, String> settings = new HashMap<>();

        List<Settings> companySettings = settingsRepository.findByCategory("company");
        for (Settings setting : companySettings) {
            settings.put(setting.getKey(), setting.getValue());
        }

        settings.putIfAbsent("companyName", "DJBC DUTY FREE");
        settings.putIfAbsent("ifu", "00000000000000");
        settings.putIfAbsent("customsNumber", "XXXX/DGD/XX");

        return settings;
    }
}
