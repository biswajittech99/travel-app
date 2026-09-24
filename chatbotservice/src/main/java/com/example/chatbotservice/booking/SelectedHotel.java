package com.example.chatbotservice.booking;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SelectedHotel(
        String supplier,

        String hotelId,

        String hotelName,

        String city,

        LocalDate checkIn,

        LocalDate checkOut,

        BigDecimal originalPrice,

        String currency,

        Integer adults,

        Integer rooms) {
}
