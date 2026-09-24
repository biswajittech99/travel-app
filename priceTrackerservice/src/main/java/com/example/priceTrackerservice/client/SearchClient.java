package com.example.priceTrackerservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;

@FeignClient(name = "search-service", url = "http://localhost:8086")
public interface SearchClient {
    @GetMapping("/api/search/{flightId}/price")
    BigDecimal getLivePrice(@PathVariable("flightId") String flightId);
}
