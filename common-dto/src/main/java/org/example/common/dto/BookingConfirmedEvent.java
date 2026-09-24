package org.example.common.dto;

public record BookingConfirmedEvent(  String bookingUid,
                                      String userId,
                                      String userEmail,
                                      String flightId,
                                      int seats,

                                      double baseFare,
                                      double baggageFee,
                                      double seatFee,
                                      double convenienceFee,
                                      double gst,

                                      double totalAmount,
                                      String pnr) {

}
