package com.example.invoiceservice.controller;

import com.example.invoiceservice.entity.Invoice;
import com.example.invoiceservice.repository.InvoiceRepository;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {
    private final InvoiceRepository invoiceRepo;

    public InvoiceController(InvoiceRepository invoiceRepo) {
        this.invoiceRepo = invoiceRepo;
    }

    /**
     * 1️⃣ Get invoice by booking UID
     * Used in: User profile → booking details
     */
    @GetMapping("/by-booking/{bookingUid}")
    public ResponseEntity<Invoice> getByBooking(@PathVariable String bookingUid) {

        return invoiceRepo.findByBookingUid(bookingUid)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 2️⃣ Download invoice PDF
     * Used in: "Download Invoice" button
     */
    @GetMapping("/{invoiceId}/download")
    public ResponseEntity<Resource> downloadInvoice(@PathVariable String invoiceId) {

        Invoice invoice = invoiceRepo.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        File file = new File(invoice.getPdfPath());
        if (!file.exists()) {
            throw new RuntimeException("Invoice PDF missing");
        }

        Resource resource = new FileSystemResource(file);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + file.getName()
                )
                .body(resource);
    }

    /**
     * 3️⃣ List all invoices of a user
     * Used in: Profile → My Invoices
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserInvoices(@PathVariable String userId) {

        return ResponseEntity.ok(
                invoiceRepo.findByUserIdOrderByCreatedAtDesc(userId)
        );
    }
}
