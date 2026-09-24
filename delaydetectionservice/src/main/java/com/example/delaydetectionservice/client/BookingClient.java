package com.example.delaydetectionservice.client;

import org.example.common.dto.BookingInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(
        name = "booking-service",
        url = "http://localhost:8087"
)
public interface BookingClient {
    @GetMapping("/api/bookings/by-flight/{flightId}")
    List<BookingInfo> findBookingsByFlight(@PathVariable String flightId);
}
