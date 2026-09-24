package com.example.delaydetectionservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name="flight_status")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightStatus {
    @Id
    private String flightId;     // "AI:4"

    private String supplier;     // "AI"
    private String status;       // ON_TIME / DELAYED / CANCELLED

    private Integer delayMinutes;

    private Instant statusAt;    // supplier reported time

    @Column(columnDefinition = "TEXT")
    private String rawPayload;   // optional JSON

    private Instant lastUpdatedAt;
}
