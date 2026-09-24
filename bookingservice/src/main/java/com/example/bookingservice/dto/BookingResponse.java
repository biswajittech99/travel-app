package com.example.bookingservice.dto;

public record BookingResponse(String bookingUid,
                              String userId,
                              String flightId,
                              int seats,
                              String status) {
}
