package com.example.delaydetectionservice.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name="delay_claims")
public class DelayClaim {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String bookingUid;
    private String flightId;
    private Integer delayMinutes;
    private String status; // CREATED/SUBMITTED/SUCCESS/FAILED
    private BigDecimal amount;
    private Instant createdAt = Instant.now();
}
