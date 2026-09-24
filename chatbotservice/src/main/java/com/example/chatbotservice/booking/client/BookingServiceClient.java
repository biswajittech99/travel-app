package com.example.chatbotservice.booking.client;

import org.example.common.dto.CreateFlightBookingRequest;
import org.example.common.dto.CreateHotelBookingRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class BookingServiceClient {

    private final WebClient webClient;

    public BookingServiceClient(
            WebClient.Builder builder
    ) {

        this.webClient =
                builder
                        .baseUrl(
                                "http://localhost:8087/api/bookings"
                        )
                        .build();
    }

    public String createFlightBooking(
            CreateFlightBookingRequest request
    ) {

        return webClient.post()

                .uri("/flights")

                .header(
                        "Idempotency-Key",
                        request.idempotencyKey()
                )

                .bodyValue(request)

                .retrieve()

                .bodyToMono(String.class)

                .block();
    }

    public String createHotelBooking(
            CreateHotelBookingRequest request
    ) {

        return webClient.post()

                .uri("/hotels")

                .header(
                        "Idempotency-Key",
                        request.idempotencyKey()
                )

                .bodyValue(request)

                .retrieve()

                .bodyToMono(String.class)

                .block();
    }
}
