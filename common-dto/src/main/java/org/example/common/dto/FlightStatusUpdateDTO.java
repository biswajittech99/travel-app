package org.example.common.dto;

import java.time.Instant;

public record FlightStatusUpdateDTO(String supplier,      // "amadeus" or "airlineX"
                                    String flightId,      // e.g., "AI:4"
                                    String supplierOfferId, // optional
                                    String status,// "SCHEDULED"|"DEPARTED"|"LANDED"|"CANCELLED"|"DIVERTED"|"DELAYED"
                                    Instant timestamp, Instant updatedAt,   // event time
                                    Integer delayMinutes, // nullable, minutes delay reported
                                    String rawPayload ) {
}
