package com.example.bookingservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "flight_inventory")
@Getter
@Setter
public class FlightInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "flight_id", unique = true, nullable = false)
    private String flightId;
    // e.g. amadeus:AI1234

    @Column(nullable = false)
    private Integer seatsAvailable;

    @Column(nullable = false)
    private Integer seatsReserved = 0;

    @Column(name = "supplier", nullable = false)
    private String supplier;   // e.g. AI (Air India), HR (Hahn Air)

    @Column(name = "from_airport", nullable = false)
    private String fromAirport; // e.g. GOI

    @Column(name = "to_airport", nullable = false)
    private String toAirport;   // e.g. BOM

    @Column(name = "departure", nullable = false)
    private LocalDateTime departure;

    @Column(name = "arrival", nullable = false)
    private LocalDateTime arrival;

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "currency", nullable = false)
    private String currency;   // e.g. INR, USD


    @Column(name = "supplier_offer_id")
    private String supplierOfferId; // optional but useful for Amadeus bookings

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    @Column(name = "is_active")
    private boolean active = true;

}
