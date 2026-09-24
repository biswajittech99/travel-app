package com.example.bookingservice.service;


import org.example.common.dto.FlightRevalidationRequest;
import org.example.common.dto.FlightRevalidationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class FlightRevalidationClient {

    private final WebClient webClient;

    public FlightRevalidationClient(
            WebClient.Builder builder,
            @Value("${services.search-url}")
            String searchUrl
    ) {

        this.webClient =
                builder
                        .baseUrl(searchUrl)
                        .build();
    }

    public FlightRevalidationResponse revalidate(
            FlightRevalidationRequest request
    ) {

        return webClient.post()
                .uri("/api/search/flights/revalidate")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(
                        FlightRevalidationResponse.class
                )
                .block();
    }
}
