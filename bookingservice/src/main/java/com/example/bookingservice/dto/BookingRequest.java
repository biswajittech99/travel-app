package com.example.bookingservice.dto;

import java.util.List;

public record BookingRequest(String userId,
                             String flightId,
                             List<String> seatNumbers,
                             double mealSelected,
                             int extraBaggageKg,
                             boolean insuranceSelected,
                             PriceSnapshot priceSnapshot,
                             String paymentMethod,
                             String userEmail) {
}
