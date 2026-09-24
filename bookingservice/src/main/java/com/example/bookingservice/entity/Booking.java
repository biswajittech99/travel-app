package com.example.bookingservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "booking")
@Getter
@Setter
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_uid", unique = true, nullable = false)
    private String bookingUid;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "flight_id", nullable = false)
    private String flightId;

    @Column(name = "seats", nullable = false)
    private int seats;

    @ElementCollection
    private List<String> seatNumbers;

    @Column(name = "status", nullable = false)
    private String status; // CREATED/CONFIRMED/CANCELLED

    @Column(name = "idempotency_key")
    private String idempotencyKey;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "price", nullable = false)
    private Double price;
    // per booking total price
    @Column(name = "user_email", nullable = false)
    private String userEmail;      // user’s email for notification

    @Column(name = "pnr")
    private String pnr;

    @Column(name = "payment_provider_id")
    private String paymentProviderId;
    @Column(name = "base_fare")
    private double baseFare;
    @Column(name = "baggage_fee")
    private double baggageFee;
    @Column(name = "meal_fee")
    private double mealFee;
    @Column(name = "insurance_fee")
    private double insuranceFee;
    @Column(name = "seat_fee")
    private double seatFee;
    @Column(name = "convenience_fee")
    private double convenienceFee;
    @Column(name = "gst")
    private double gst;
    @Column(name = "total_Amount")
    private double totalAmount;
    @Column(name = "expires_At")
    private LocalDateTime expiresAt;



}
