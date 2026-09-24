package org.example.common.dto;

public record BookingFailedEvent(String bookingUid,
                                 String userId,
                                 String userEmail,
                                 String flightId,
                                 String reason) {
}
