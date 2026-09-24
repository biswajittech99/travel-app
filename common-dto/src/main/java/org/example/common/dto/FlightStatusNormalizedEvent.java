package org.example.common.dto;

import java.time.Instant;

public record FlightStatusNormalizedEvent(String flightId,
                                          String supplier,
                                          String status,
                                          Instant statusAt,
                                          Integer delayMinutes, String rawPayload) {
}
