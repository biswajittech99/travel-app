package com.example.searchservice.request;

import java.time.LocalDate;

public record HotelSearchRequest(String cityCode,

                                 LocalDate checkIn,

                                 LocalDate checkOut,

                                 Integer adults,

                                 Integer rooms) {
}
