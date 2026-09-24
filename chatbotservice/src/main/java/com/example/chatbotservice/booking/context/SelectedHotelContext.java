package com.example.chatbotservice.booking.context;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SelectedHotelContext(String hotelId,

                                   String hotelName,

                                   String city,

                                   LocalDate checkIn,

                                   LocalDate checkOut,

                                   Integer adults,

                                   Integer rooms,

                                   BigDecimal originalPrice,


                                   String currency) {
}
