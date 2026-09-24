package com.example.chatbotservice.booking.client;

import org.example.common.dto.FlightRevalidationRequest;
import org.example.common.dto.FlightRevalidationResponse;
import org.example.common.dto.HotelRevalidationRequest;
import org.example.common.dto.HotelRevalidationResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class SearchServiceClient {

    private final WebClient webClient;

    public SearchServiceClient(
            WebClient.Builder builder
    ) {

        this.webClient =
                builder
                        .baseUrl(
                                "http://localhost:8086/api/search"
                        )
                        .build();
    }

    public FlightRevalidationResponse revalidateFlight(
            FlightRevalidationRequest request
    ) {

        return webClient.post()

                .uri("/flights/revalidate")

                .bodyValue(request)

                .retrieve()

                .bodyToMono(
                        FlightRevalidationResponse.class
                )

                .block();
    }

    public HotelRevalidationResponse revalidateHotel(
            HotelRevalidationRequest request
    ) {

        return webClient.post()

                .uri("/hotels/revalidate")

                .bodyValue(request)

                .retrieve()

                .bodyToMono(
                        HotelRevalidationResponse.class
                )

                .block();
    }
}
