package org.example.common.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record FlightRevalidationRequest(String userId,

                                        String supplier,

                                        String flightId,

                                        String supplierOfferId,

                                        String flightNumber,

                                        String origin,

                                        String destination,

                                        Instant departure,

                                        BigDecimal originalPrice,

                                        String currency,

                                        Integer passengers) {
}
