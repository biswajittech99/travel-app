package com.example.invoiceservice.service;

import com.example.invoiceservice.entity.Invoice;
import com.example.invoiceservice.repository.InvoiceRepository;
import org.example.common.dto.BookingConfirmedEvent;
import org.example.common.dto.InvoiceGeneratedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class InvoiceService {

    private final InvoiceRepository repo;
    private final PdfGenerator pdfGenerator;
    private final InvoiceEventProducer producer;
    private final Logger log = LoggerFactory.getLogger(getClass());

    public InvoiceService(
            InvoiceRepository repo,
            PdfGenerator pdfGenerator,
            InvoiceEventProducer producer
    ) {
        this.repo = repo;
        this.pdfGenerator = pdfGenerator;
        this.producer = producer;
    }

    @Transactional
    public void generateInvoice(BookingConfirmedEvent e) {

        if (repo.findByBookingUid(e.bookingUid()).isPresent()) {
            log.warn("⚠️ Invoice already exists for booking {}", e.bookingUid());
            return;
        }

        // 1️⃣ Create invoice
        Invoice invoice = new Invoice();
        invoice.setBookingUid(e.bookingUid());
        invoice.setUserId(e.userId());
        invoice.setFlightId(e.flightId());
        invoice.setUserEmail(e.userEmail());

        invoice.setBaseFare(e.baseFare());
        invoice.setBaggageFee(e.baggageFee());
        invoice.setSeatFee(e.seatFee());
        invoice.setConvenienceFee(e.convenienceFee());
        invoice.setGst(e.gst());
        invoice.setPnr(e.pnr());
        invoice.setTotalAmount(e.totalAmount());

        invoice.setCreatedAt(Instant.now());

        // 2️⃣ Generate PDF FIRST
        String pdfPath = pdfGenerator.generateInvoicePdf(invoice);
        invoice.setPdfPath(pdfPath);

        // 3️⃣ Save only once (clean DB state)
        repo.save(invoice);
        log.info("🧾 Invoice generated successfully for booking {}", e.bookingUid());

        // 4️⃣ Publish invoice-generated event
        producer.publish(
                new InvoiceGeneratedEvent(
                        invoice.getInvoiceId(),
                        invoice.getBookingUid(),
                        invoice.getUserEmail(),
                        pdfPath
                )
        );
    }
}
