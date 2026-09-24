package com.example.invoiceservice.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(name = "invoices")
@Data
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "invoice_id", updatable = false, nullable = false)
    private String invoiceId;
    @Column(nullable = false, unique = true)
    private String bookingUid;
    @Column(name = "user_id", nullable = false)
    private String userId;
    @Column(name = "user_email", nullable = false)
    private String userEmail;
    @Column(name = "flight_id", nullable = false)
    private String flightId;
    @Column(name = "pnr")
    private String pnr;
    private double baseFare;
    private double baggageFee;
    private double seatFee;
    private double convenienceFee;
    private double gst;
    private double totalAmount;
    private String pdfPath;
    private Instant createdAt = Instant.now();
    @Version
    private Long version;
}
