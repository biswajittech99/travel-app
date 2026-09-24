package org.example.common.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record FlightRevalidationResponse( boolean available,

                                          boolean priceChanged,

                                          BigDecimal originalPrice,

                                          BigDecimal currentPrice,

                                          String currency,

                                          String flightNumber,

                                          String message,

                                          Instant revalidatedAt) {
}
