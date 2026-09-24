package com.example.searchservice.request;

import com.example.searchservice.enums.TravelType;

import java.time.LocalDate;

public record TravelSearchRequest( TravelType travelType,
                                   String origin,
                                   String destination,
                                   LocalDate departureDate,
                                   LocalDate returnDate,
                                   Integer adults,
                                   Integer children,
                                   Integer rooms) {
}
