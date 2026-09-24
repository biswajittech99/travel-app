package org.example.common.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record HotelRevalidationResponse( boolean available,

                                           boolean priceChanged,

                                           BigDecimal originalPrice,

                                           BigDecimal currentPrice,

                                           String hotelId,

                                           String hotelName,

                                           LocalDate checkIn,

                                           LocalDate checkOut,

                                           String currency,

                                           Integer availableRooms,

                                           String message) {
}
