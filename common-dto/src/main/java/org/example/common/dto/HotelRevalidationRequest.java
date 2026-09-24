package org.example.common.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record HotelRevalidationRequest( String hotelId,
                                        String hotelName,
                                        String city,
                                        LocalDate checkIn,
                                        LocalDate checkOut,
                                        Integer adults,
                                        Integer rooms,
                                        BigDecimal originalPrice,
                                        String currency){
}
