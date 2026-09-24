package org.example.common.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateHotelBookingRequest(String userId,

                                        String idempotencyKey,

                                        String supplier,

                                        String hotelId,

                                        String hotelName,

                                        String city,

                                        LocalDate checkIn,

                                        LocalDate checkOut,

                                        BigDecimal price,

                                        String currency,

                                        Integer adults,

                                        Integer rooms,

                                        String paymentMethod,

                                        String userEmail) {
}
