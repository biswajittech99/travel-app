package com.example.searchservice.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record TravelOption( String supplier, String id,
                            String from, String to,
                            String flightNumber,    // ✅ NEW: "AI302", "6E201"
                            String cabinClass,
                            Instant departure,    // ← Instant type
                            Instant arrival,      // ← Instant type
                            BigDecimal price,
                            String currency,
                            int seatsAvailable) {
}
