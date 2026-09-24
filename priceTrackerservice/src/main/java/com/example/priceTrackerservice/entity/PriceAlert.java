package com.example.priceTrackerservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "price_alerts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceAlert {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String userId;

    private String userEmail;

    // Example: DEL-BLR-2025-02-10
    private String searchKey;

    // Example: AI-203
    private String flightId;

    @Column(precision = 10, scale = 2)
    private BigDecimal targetPrice;

    private boolean active = true;

    // Advanced features
    private boolean autoRebook;
    private String preferredPaymentMethod;
}
