package com.example.bookingservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "hotel_booking")
@Getter
@Setter
public class HotelBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_uid", unique = true, nullable = false)
    private String bookingUid;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "hotel_id", nullable = false)
    private String hotelId;

    @Column(name = "hotel_name")
    private String hotelName;

    @Column(name = "city")
    private String city;

    @Column(name = "check_in", nullable = false)
    private LocalDate checkIn;

    @Column(name = "check_out", nullable = false)
    private LocalDate checkOut;

    @Column(nullable = false)
    private int rooms;

    @Column(nullable = false)
    private int adults;

    @Column(name = "status", nullable = false)
    private String status; // PAYMENT_PENDING / CONFIRMED / PAYMENT_FAILED / EXPIRED

    @Column(name = "idempotency_key")
    private String idempotencyKey;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();

    // total for the whole stay = pricePerNight * nights * rooms + fees
    @Column(name = "price_per_night")
    private double pricePerNight;

    @Column(name = "convenience_fee")
    private double convenienceFee;

    @Column(name = "gst")
    private double gst;

    @Column(name = "total_amount", nullable = false)
    private double totalAmount;

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @Column(name = "pnr")
    private String pnr; // reused as booking confirmation code

    @Column(name = "payment_provider_id")
    private String paymentProviderId;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
}
