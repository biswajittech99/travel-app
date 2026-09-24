package com.example.chatbotservice.rag.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record TravelOption(String supplier,

                           String id,

                           String from,

                           String to,

                           String flightNumber,

                           String cabinClass,

                           Instant departure,
                           Instant arrival,

                           BigDecimal price,

                           String currency,

                           Integer seatsAvailable) {
}
