package com.example.bookingservice.dto;

public record PriceSnapshot( double baseFare,
                             double seatFee,
                             double baggageFee,
                             double mealFee,
                             double insuranceFee,
                             double convenienceFee,
                             double gst,
                             double totalAmount) {
}
