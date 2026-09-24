package com.example.chatbotservice.booking;

import java.math.BigDecimal;
import java.time.Instant;

public record SelectedFlight(String supplier,

                             String flightId,

                             String supplierOfferId,

                             String flightNumber,

                             String origin,

                             String destination,

                             Instant departure,

                             Instant arrival,

                             BigDecimal price,

                             String currency,

                             Integer passengers) {
}
