package com.example.searchservice.dto;

import java.util.List;

public record TravelSearchResponse(List<TravelOption> flights,

                                   List<HotelOption> hotels) {
}
