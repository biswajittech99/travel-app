package com.example.bookingservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "booking_audit")
@Getter
@Setter
@NoArgsConstructor
public class BookingAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_uid", nullable = false)
    private String bookingUid;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "flight_id", nullable = false)
    private String flightId;

    @Column(name = "seats", nullable = false)
    private int seats;

    @Column(name = "success", nullable = false)
    private boolean success;

    @Column(name = "reason")
    private String reason;

    private LocalDateTime createdAt = LocalDateTime.now();

    public BookingAudit(
            String bookingUid,
            String userId,
            String flightId,
            int seats,
            boolean success,
            String reason
    ) {
        this.bookingUid = bookingUid;
        this.userId = userId;
        this.flightId = flightId;
        this.seats = seats;
        this.success = success;
        this.reason = reason;
    }
}
