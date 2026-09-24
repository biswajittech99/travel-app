package com.example.chatbotservice.booking.context;

import java.math.BigDecimal;

public record SelectedFlightContext(String flightId,

                                    String supplier,

                                    String supplierOfferId,

                                    String flightNumber,

                                    String origin,

                                    String destination,

                                    java.time.Instant departure,

                                    BigDecimal displayedPrice,

                                    String currency,

                                    Integer passengers) {
}
