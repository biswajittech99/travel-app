package com.example.invoiceservice.service;

import com.example.invoiceservice.entity.Invoice;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component
public class PdfGenerator {
    public String generateInvoicePdf(Invoice invoice) {

        String dirPath = "invoices";
        String filePath = dirPath + "/invoice-" + invoice.getBookingUid() + ".pdf";

        try {
            // ✅ Ensure directory exists
            Files.createDirectories(Paths.get(dirPath));

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(filePath));

            document.open();

            document.add(new Paragraph("✈ Flight Invoice"));
            document.add(new Paragraph("Booking ID: " + invoice.getBookingUid()));
            document.add(new Paragraph("Flight ID: " + invoice.getFlightId()));
            document.add(new Paragraph("PNR: " + invoice.getPnr()));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Base Fare: ₹" + invoice.getBaseFare()));
            document.add(new Paragraph("Baggage Fee: ₹" + invoice.getBaggageFee()));
            document.add(new Paragraph("Seat Fee: ₹" + invoice.getSeatFee()));
            document.add(new Paragraph("Convenience Fee: ₹" + invoice.getConvenienceFee()));
            document.add(new Paragraph("GST: ₹" + invoice.getGst()));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("TOTAL: ₹" + invoice.getTotalAmount()));

            document.close();

            return filePath;

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate invoice PDF", e);
        }
    }
}
