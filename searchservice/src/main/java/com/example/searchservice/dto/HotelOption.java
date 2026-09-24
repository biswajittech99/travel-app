package com.example.searchservice.dto;

import java.math.BigDecimal;
import java.util.List;

public record HotelOption(String supplier,

                          String hotelId,

                          String hotelName,

                          String city,

                          String address,

                          Double latitude,

                          Double longitude,

                          BigDecimal price,

                          String currency,

                          Integer availableRooms,

                          List<String> amenities,

                          List<String> images,

                          Double rating
) {

}
