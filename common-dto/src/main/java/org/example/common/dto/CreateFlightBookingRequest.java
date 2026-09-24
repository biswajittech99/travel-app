package org.example.common.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record CreateFlightBookingRequest(String userId,

                                         String idempotencyKey,

                                         String supplier,

                                         String flightId,

                                         String supplierOfferId,

                                         String flightNumber,

                                         String origin,

                                         String destination,

                                         Instant departure,

                                         BigDecimal price,

                                         String currency,

                                         Integer passengers,

                                         String paymentMethod,

                                         String userEmail) {
}
