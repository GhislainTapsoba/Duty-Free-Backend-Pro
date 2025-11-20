package com.djbc.dutyfree.util;

import com.djbc.dutyfree.domain.entity.*;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.io.font.constants.StandardFonts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ReceiptGenerator {

    private static final int RECEIPT_WIDTH = 48;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Color scheme for PDF
    private static final DeviceRgb HEADER_COLOR = new DeviceRgb(41, 128, 185); // Blue
    private static final DeviceRgb BORDER_COLOR = new DeviceRgb(189, 195, 199); // Light gray

    /**
     * Generate a fiscally compliant PDF receipt
     *
     * @param receipt Receipt entity with sale information
     * @param companySettings Map containing company settings (IFU, RCCM, etc.)
     * @param outputPath Path where the PDF will be saved
     * @return Path to the generated PDF file
     * @throws Exception if PDF generation fails
     */
    public String generatePDF(Receipt receipt, Map<String, String> companySettings, String outputPath) throws Exception {
        log.info("Generating PDF receipt for receipt number: {}", receipt.getReceiptNumber());

        // Ensure output directory exists
        Path path = Paths.get(outputPath);
        Files.createDirectories(path.getParent());

        // Create PDF document
        PdfWriter writer = new PdfWriter(new FileOutputStream(outputPath));
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc, PageSize.A4);
        document.setMargins(20, 20, 20, 20);

        // Load fonts
        PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        Sale sale = receipt.getSale();

        // Add header with company information
        addHeader(document, boldFont, regularFont, companySettings);

        // Add receipt metadata
        addReceiptMetadata(document, boldFont, regularFont, receipt, sale);

        // Add customer/passenger information
        if (sale.getPassengerName() != null) {
            addPassengerInfo(document, boldFont, regularFont, sale);
        }

        // Add items table
        addItemsTable(document, boldFont, regularFont, sale.getItems());

        // Add totals section
        addTotalsSection(document, boldFont, regularFont, sale);

        // Add payment information
        addPaymentInfo(document, boldFont, regularFont, sale.getPayments());

        // Add fiscal mentions
        addFiscalMentions(document, regularFont, companySettings);

        // Add QR code for verification
        addQRCode(document, receipt, sale);

        // Add footer
        addFooter(document, regularFont, companySettings);

        document.close();

        log.info("PDF receipt generated successfully at: {}", outputPath);
        return outputPath;
    }

    private void addHeader(Document document, PdfFont boldFont, PdfFont regularFont, Map<String, String> settings) {
        // Add logo if available
        String logoPath = settings.get("receipt.logo.path");
        if (logoPath != null && !logoPath.isEmpty()) {
            try {
                Path logoFile = Paths.get(logoPath);
                if (Files.exists(logoFile)) {
                    com.itextpdf.layout.element.Image logo = new com.itextpdf.layout.element.Image(
                            com.itextpdf.io.image.ImageDataFactory.create(logoFile.toString())
                    );
                    logo.setWidth(120);
                    logo.setHeight(60);
                    logo.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);
                    logo.setMarginBottom(10);
                    document.add(logo);
                }
            } catch (Exception e) {
                log.warn("Could not load logo from path: {}", logoPath, e);
            }
        }

        // Custom header message if available
        String customHeader = settings.get("receipt.header.message");
        if (customHeader != null && !customHeader.isEmpty()) {
            Paragraph headerMsg = new Paragraph(customHeader)
                    .setFont(regularFont)
                    .setFontSize(9)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setItalic()
                    .setMarginBottom(10);
            document.add(headerMsg);
        }

        // Company name
        Paragraph companyName = new Paragraph(settings.getOrDefault("companyName", "DJBC DUTY FREE"))
                .setFont(boldFont)
                .setFontSize(18)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(HEADER_COLOR)
                .setMarginBottom(5);
        document.add(companyName);

        // Company address
        Paragraph address = new Paragraph(settings.getOrDefault("companyAddress", "Aéroport International de Ouagadougou"))
                .setFont(regularFont)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(2);
        document.add(address);

        // Phone and email
        String phone = settings.getOrDefault("companyPhone", "+226 XX XX XX XX");
        String email = settings.getOrDefault("companyEmail", "contact@djbc-dutyfree.bf");
        Paragraph contact = new Paragraph(String.format("Tél: %s | Email: %s", phone, email))
                .setFont(regularFont)
                .setFontSize(9)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10);
        document.add(contact);

        // Fiscal information (IFU, RCCM, etc.)
        Table fiscalTable = new Table(UnitValue.createPercentArray(new float[]{1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(15);

        String ifu = settings.getOrDefault("ifu", "00000000000000");
        String rccm = settings.getOrDefault("rccm", "BF-OUA-XXXXXX");
        String customsNumber = settings.getOrDefault("customsNumber", "XXXX/DGD/XX");

        fiscalTable.addCell(createCell(
                String.format("IFU: %s | RCCM: %s | N° Agrément Douane: %s", ifu, rccm, customsNumber),
                regularFont, 8, TextAlignment.CENTER, false, true
        ));

        document.add(fiscalTable);

        // Separator line
        document.add(new Paragraph()
                .setBorder(new SolidBorder(BORDER_COLOR, 1))
                .setMarginBottom(10));
    }

    private void addReceiptMetadata(Document document, PdfFont boldFont, PdfFont regularFont,
                                   Receipt receipt, Sale sale) {
        // Receipt title
        Paragraph title = new Paragraph("TICKET DE CAISSE / RECEIPT")
                .setFont(boldFont)
                .setFontSize(14)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(15);
        document.add(title);

        // Metadata table
        Table metaTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(10);

        metaTable.addCell(createCell("N° Ticket:", boldFont, 9, TextAlignment.LEFT, false, false));
        metaTable.addCell(createCell(receipt.getReceiptNumber(), regularFont, 9, TextAlignment.RIGHT, false, false));

        metaTable.addCell(createCell("N° Vente:", boldFont, 9, TextAlignment.LEFT, false, false));
        metaTable.addCell(createCell(sale.getSaleNumber(), regularFont, 9, TextAlignment.RIGHT, false, false));

        metaTable.addCell(createCell("Date:", boldFont, 9, TextAlignment.LEFT, false, false));
        metaTable.addCell(createCell(sale.getSaleDate().format(DATE_TIME_FORMATTER), regularFont, 9, TextAlignment.RIGHT, false, false));

        metaTable.addCell(createCell("Caissier:", boldFont, 9, TextAlignment.LEFT, false, false));
        metaTable.addCell(createCell(sale.getCashier().getFullName(), regularFont, 9, TextAlignment.RIGHT, false, false));

        metaTable.addCell(createCell("Caisse N°:", boldFont, 9, TextAlignment.LEFT, false, false));
        metaTable.addCell(createCell(String.valueOf(sale.getCashRegister().getId()), regularFont, 9, TextAlignment.RIGHT, false, false));

        document.add(metaTable);

        // Separator
        document.add(new Paragraph()
                .setBorder(new SolidBorder(BORDER_COLOR, 0.5f))
                .setMarginTop(5)
                .setMarginBottom(10));
    }

    private void addPassengerInfo(Document document, PdfFont boldFont, PdfFont regularFont, Sale sale) {
        Paragraph sectionTitle = new Paragraph("INFORMATIONS PASSAGER / PASSENGER INFORMATION")
                .setFont(boldFont)
                .setFontSize(10)
                .setMarginBottom(5);
        document.add(sectionTitle);

        Table passengerTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(10);

        if (sale.getPassengerName() != null) {
            passengerTable.addCell(createCell("Nom / Name:", boldFont, 9, TextAlignment.LEFT, false, false));
            passengerTable.addCell(createCell(sale.getPassengerName(), regularFont, 9, TextAlignment.RIGHT, false, false));
        }

        if (sale.getFlightNumber() != null) {
            passengerTable.addCell(createCell("Vol / Flight:", boldFont, 9, TextAlignment.LEFT, false, false));
            passengerTable.addCell(createCell(sale.getFlightNumber(), regularFont, 9, TextAlignment.RIGHT, false, false));
        }

        if (sale.getAirline() != null) {
            passengerTable.addCell(createCell("Compagnie / Airline:", boldFont, 9, TextAlignment.LEFT, false, false));
            passengerTable.addCell(createCell(sale.getAirline(), regularFont, 9, TextAlignment.RIGHT, false, false));
        }

        if (sale.getDestination() != null) {
            passengerTable.addCell(createCell("Destination:", boldFont, 9, TextAlignment.LEFT, false, false));
            passengerTable.addCell(createCell(sale.getDestination(), regularFont, 9, TextAlignment.RIGHT, false, false));
        }

        document.add(passengerTable);

        // Separator
        document.add(new Paragraph()
                .setBorder(new SolidBorder(BORDER_COLOR, 0.5f))
                .setMarginTop(5)
                .setMarginBottom(10));
    }

    private void addItemsTable(Document document, PdfFont boldFont, PdfFont regularFont, List<SaleItem> items) {
        Paragraph sectionTitle = new Paragraph("ARTICLES / ITEMS")
                .setFont(boldFont)
                .setFontSize(10)
                .setMarginBottom(5);
        document.add(sectionTitle);

        // Items table with columns: Description, Qty, Unit Price, Discount, Tax, Total
        Table itemsTable = new Table(UnitValue.createPercentArray(new float[]{3, 1, 1.5f, 1, 1, 1.5f}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(10);

        // Header row
        itemsTable.addHeaderCell(createCell("Description", boldFont, 9, TextAlignment.LEFT, true, false));
        itemsTable.addHeaderCell(createCell("Qté", boldFont, 9, TextAlignment.CENTER, true, false));
        itemsTable.addHeaderCell(createCell("P.U.", boldFont, 9, TextAlignment.RIGHT, true, false));
        itemsTable.addHeaderCell(createCell("Rem.", boldFont, 9, TextAlignment.RIGHT, true, false));
        itemsTable.addHeaderCell(createCell("TVA", boldFont, 9, TextAlignment.RIGHT, true, false));
        itemsTable.addHeaderCell(createCell("Total", boldFont, 9, TextAlignment.RIGHT, true, false));

        // Item rows
        for (SaleItem item : items) {
            Product product = item.getProduct();

            // Product name
            itemsTable.addCell(createCell(product.getNameFr(), regularFont, 8, TextAlignment.LEFT, false, false));

            // Quantity
            itemsTable.addCell(createCell(String.valueOf(item.getQuantity()), regularFont, 8, TextAlignment.CENTER, false, false));

            // Unit price
            itemsTable.addCell(createCell(formatAmount(item.getUnitPrice()), regularFont, 8, TextAlignment.RIGHT, false, false));

            // Discount
            itemsTable.addCell(createCell(formatAmount(item.getDiscount()), regularFont, 8, TextAlignment.RIGHT, false, false));

            // Tax
            itemsTable.addCell(createCell(formatAmount(item.getTaxAmount()), regularFont, 8, TextAlignment.RIGHT, false, false));

            // Total
            itemsTable.addCell(createCell(formatAmount(item.getTotalPrice()), regularFont, 8, TextAlignment.RIGHT, false, false));
        }

        document.add(itemsTable);
    }

    private void addTotalsSection(Document document, PdfFont boldFont, PdfFont regularFont, Sale sale) {
        // Separator
        document.add(new Paragraph()
                .setBorder(new SolidBorder(BORDER_COLOR, 1))
                .setMarginTop(5)
                .setMarginBottom(10));

        Table totalsTable = new Table(UnitValue.createPercentArray(new float[]{3, 1}))
                .setWidth(UnitValue.createPercentValue(60))
                .setMarginBottom(10);

        // Subtotal (HT - Hors Taxes)
        totalsTable.addCell(createCell("Sous-total HT / Subtotal:", regularFont, 9, TextAlignment.LEFT, false, false));
        totalsTable.addCell(createCell(formatAmount(sale.getSubtotal()), regularFont, 9, TextAlignment.RIGHT, false, false));

        // Discount
        if (sale.getDiscount().compareTo(BigDecimal.ZERO) > 0) {
            totalsTable.addCell(createCell("Remise / Discount:", regularFont, 9, TextAlignment.LEFT, false, false));
            totalsTable.addCell(createCell("-" + formatAmount(sale.getDiscount()), regularFont, 9, TextAlignment.RIGHT, false, false));
        }

        // Tax (TVA)
        totalsTable.addCell(createCell("TVA / Tax:", regularFont, 9, TextAlignment.LEFT, false, false));
        totalsTable.addCell(createCell(formatAmount(sale.getTaxAmount()), regularFont, 9, TextAlignment.RIGHT, false, false));

        // Total (TTC - Toutes Taxes Comprises)
        totalsTable.addCell(createCell("TOTAL TTC / TOTAL:", boldFont, 11, TextAlignment.LEFT, true, false)
                .setBackgroundColor(new DeviceRgb(236, 240, 241)));
        totalsTable.addCell(createCell(formatAmount(sale.getTotalAmount()), boldFont, 11, TextAlignment.RIGHT, true, false)
                .setBackgroundColor(new DeviceRgb(236, 240, 241)));

        document.add(totalsTable);
    }

    private void addPaymentInfo(Document document, PdfFont boldFont, PdfFont regularFont, List<Payment> payments) {
        if (payments == null || payments.isEmpty()) {
            return;
        }

        Paragraph sectionTitle = new Paragraph("MOYENS DE PAIEMENT / PAYMENT METHODS")
                .setFont(boldFont)
                .setFontSize(10)
                .setMarginTop(10)
                .setMarginBottom(5);
        document.add(sectionTitle);

        Table paymentTable = new Table(UnitValue.createPercentArray(new float[]{2, 1}))
                .setWidth(UnitValue.createPercentValue(60))
                .setMarginBottom(10);

        for (Payment payment : payments) {
            String methodLabel = switch (payment.getPaymentMethod()) {
                case CASH -> "Espèces / Cash";
                case CARD -> "Carte / Card";
                case MOBILE_MONEY -> "Mobile Money";
                case MIXED -> "Mixte / Mixed";
                default -> payment.getPaymentMethod().toString();
            };

            paymentTable.addCell(createCell(methodLabel, regularFont, 9, TextAlignment.LEFT, false, false));
            paymentTable.addCell(createCell(formatAmount(payment.getAmountInXOF()), regularFont, 9, TextAlignment.RIGHT, false, false));
        }

        document.add(paymentTable);
    }

    private void addFiscalMentions(Document document, PdfFont regularFont, Map<String, String> settings) {
        // Separator
        document.add(new Paragraph()
                .setBorder(new SolidBorder(BORDER_COLOR, 1))
                .setMarginTop(15)
                .setMarginBottom(10));

        Paragraph fiscalTitle = new Paragraph("MENTIONS LÉGALES / LEGAL NOTICES")
                .setFont(regularFont)
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER)
                .setBold()
                .setMarginBottom(5);
        document.add(fiscalTitle);

        String[] legalMentions = {
            "Ce ticket tient lieu de facture conformément à l'article X de la loi fiscale.",
            "This receipt serves as an invoice in accordance with article X of the tax law.",
            "",
            "Vente en franchise de taxes / Tax-free sale",
            "Articles vendus sous le régime douanier 4200 (Boutique sous douane)",
            "Goods sold under customs regime 4200 (Duty Free Shop)",
            "",
            "Conservation obligatoire pour contrôle douanier",
            "Mandatory retention for customs control"
        };

        for (String mention : legalMentions) {
            Paragraph p = new Paragraph(mention)
                    .setFont(regularFont)
                    .setFontSize(7)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(2);
            document.add(p);
        }
    }

    private void addQRCode(Document document, Receipt receipt, Sale sale) throws Exception {
        // Generate QR code data
        String qrData = String.format(
            "DJBC-DF|%s|%s|%s|%s",
            receipt.getReceiptNumber(),
            sale.getSaleDate().format(DATE_TIME_FORMATTER),
            sale.getTotalAmount().toString(),
            sale.getSaleNumber()
        );

        // Generate QR code image
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.MARGIN, 1);

        BitMatrix bitMatrix = new MultiFormatWriter().encode(
            qrData,
            BarcodeFormat.QR_CODE,
            150,
            150,
            hints
        );

        BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

        // Convert to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(qrImage, "PNG", baos);

        // Add QR code to document
        com.itextpdf.layout.element.Image qrCodeImage = new com.itextpdf.layout.element.Image(
            com.itextpdf.io.image.ImageDataFactory.create(baos.toByteArray())
        );
        qrCodeImage.setWidth(100);
        qrCodeImage.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);
        qrCodeImage.setMarginTop(10);

        document.add(qrCodeImage);

        // QR code label
        Paragraph qrLabel = new Paragraph("Scannez pour vérifier / Scan to verify")
                .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))
                .setFontSize(7)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(5);
        document.add(qrLabel);
    }

    private void addFooter(Document document, PdfFont regularFont) {
        addFooter(document, regularFont, new HashMap<>());
    }

    private void addFooter(Document document, PdfFont regularFont, Map<String, String> settings) {
        // Use custom footer message if available
        String footerMessage = settings.getOrDefault("receipt.footer.message",
                "Merci de votre visite / Thank you for your visit");

        Paragraph footer = new Paragraph(footerMessage)
                .setFont(regularFont)
                .setFontSize(9)
                .setTextAlignment(TextAlignment.CENTER)
                .setBold()
                .setMarginTop(15);
        document.add(footer);

        // Add custom secondary message if available
        String secondaryMessage = settings.get("receipt.footer.secondary");
        if (secondaryMessage != null && !secondaryMessage.isEmpty()) {
            Paragraph secondary = new Paragraph(secondaryMessage)
                    .setFont(regularFont)
                    .setFontSize(8)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setItalic()
                    .setMarginTop(5);
            document.add(secondary);
        }

        Paragraph timestamp = new Paragraph("Imprimé le / Printed on: " + LocalDateTime.now().format(DATE_TIME_FORMATTER))
                .setFont(regularFont)
                .setFontSize(7)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(5);
        document.add(timestamp);
    }

    private Cell createCell(String content, PdfFont font, float fontSize, TextAlignment alignment,
                           boolean isBold, boolean hasBottomBorder) {
        Paragraph p = new Paragraph(content)
                .setFont(font)
                .setFontSize(fontSize);

        if (isBold) {
            p.setBold();
        }

        Cell cell = new Cell().add(p)
                .setTextAlignment(alignment)
                .setBorder(Border.NO_BORDER)
                .setPadding(3);

        if (hasBottomBorder) {
            cell.setBorderBottom(new SolidBorder(BORDER_COLOR, 1));
        }

        return cell;
    }

    private String formatAmount(BigDecimal amount) {
        if (amount == null) {
            return "0.00";
        }
        return amount.setScale(2, RoundingMode.HALF_UP).toString() + " FCFA";
    }

    /**
     * Generate receipt text for thermal printing
     */
    public String generateReceiptText(Sale sale, String receiptNumber) {
        return generateReceiptText(sale, receiptNumber, new HashMap<>());
    }

    /**
     * Generate receipt text for thermal printing with custom settings
     */
    public String generateReceiptText(Sale sale, String receiptNumber, Map<String, String> settings) {
        StringBuilder receipt = new StringBuilder();

        // Custom header message if available
        String customHeader = settings.get("receipt.header.message");
        if (customHeader != null && !customHeader.isEmpty()) {
            receipt.append(center(customHeader)).append("\n");
        }

        // Header
        String companyName = settings.getOrDefault("companyName", "DUTY FREE");
        String companyAddress = settings.getOrDefault("companyAddress", "AEROPORT INTERNATIONAL DE OUAGADOUGOU");
        receipt.append(center(companyName)).append("\n");
        receipt.append(center(companyAddress)).append("\n");
        receipt.append(center("Burkina Faso")).append("\n");
        receipt.append(line()).append("\n");

        // Sale info
        receipt.append(leftRight("Date:", sale.getSaleDate().format(DATE_TIME_FORMATTER))).append("\n");
        receipt.append(leftRight("Ticket N°:", sale.getSaleNumber())).append("\n");
        receipt.append(leftRight("Receipt N°:", receiptNumber)).append("\n");
        receipt.append(leftRight("Cashier:", sale.getCashier().getFullName())).append("\n");
        receipt.append(leftRight("Register:", sale.getCashRegister().getRegisterNumber())).append("\n");

        if (sale.getCustomer() != null) {
            receipt.append(leftRight("Customer:",
                    sale.getCustomer().getFirstName() + " " + sale.getCustomer().getLastName())).append("\n");
        }

        receipt.append(line()).append("\n");

        // Items
        for (SaleItem item : sale.getItems()) {
            receipt.append(item.getProduct().getNameFr()).append("\n");
            receipt.append(leftRight(
                    String.format("  %d x %s", item.getQuantity(),
                            CurrencyUtil.formatXOF(item.getUnitPrice())),
                    CurrencyUtil.formatXOF(item.getTotalPrice())
            )).append("\n");

            if (item.getDiscount().compareTo(java.math.BigDecimal.ZERO) > 0) {
                receipt.append(leftRight("  Discount:",
                        "-" + CurrencyUtil.formatXOF(item.getDiscount()))).append("\n");
            }
        }

        receipt.append(line()).append("\n");

        // Totals
        receipt.append(leftRight("Subtotal:", CurrencyUtil.formatXOF(sale.getSubtotal()))).append("\n");

        if (sale.getDiscount().compareTo(java.math.BigDecimal.ZERO) > 0) {
            receipt.append(leftRight("Discount:",
                    "-" + CurrencyUtil.formatXOF(sale.getDiscount()))).append("\n");
        }

        receipt.append(leftRight("Tax:", CurrencyUtil.formatXOF(sale.getTaxAmount()))).append("\n");
        receipt.append(leftRight("TOTAL:", CurrencyUtil.formatXOF(sale.getTotalAmount()))).append("\n");
        receipt.append(line()).append("\n");

        // Payments
        receipt.append(center("PAYMENTS")).append("\n");
        sale.getPayments().forEach(payment -> {
            receipt.append(leftRight(
                    payment.getPaymentMethod().name() + ":",
                    CurrencyUtil.formatXOF(payment.getAmountInXOF())
            )).append("\n");
        });

        receipt.append(line()).append("\n");

        // Footer
        String footerMessage = settings.getOrDefault("receipt.footer.message",
                "Merci pour votre visite / Thank you for your visit");
        String secondaryMessage = settings.get("receipt.footer.secondary");

        receipt.append(center(footerMessage)).append("\n");
        if (secondaryMessage != null && !secondaryMessage.isEmpty()) {
            receipt.append(center(secondaryMessage)).append("\n");
        } else {
            receipt.append(center("A bientôt - See you soon")).append("\n");
        }
        receipt.append(line()).append("\n");

        return receipt.toString();
    }

    private String center(String text) {
        int padding = (RECEIPT_WIDTH - text.length()) / 2;
        return " ".repeat(Math.max(0, padding)) + text;
    }

    private String leftRight(String left, String right) {
        int spaces = RECEIPT_WIDTH - left.length() - right.length();
        return left + " ".repeat(Math.max(0, spaces)) + right;
    }

    private String line() {
        return "=".repeat(RECEIPT_WIDTH);
    }
}