package org.example.common.dto;

public record BookingInfo(String bookingUid,
                          String userId,
                          String userEmail,String flightId,String pnr
                          ) {
}
