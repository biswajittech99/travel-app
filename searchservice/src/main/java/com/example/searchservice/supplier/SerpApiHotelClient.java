package com.example.searchservice.supplier;


import com.example.searchservice.dto.HotelOption;
import com.example.searchservice.enums.TravelType;
import com.example.searchservice.mapper.SerpApiHotelMapper;
import com.example.searchservice.request.TravelSearchRequest;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.common.dto.HotelRevalidationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;



@Component
public class SerpApiHotelClient {

    private final Logger log =
            LoggerFactory.getLogger(SerpApiHotelClient.class);

    private final WebClient webClient;

    private final SerpApiHotelMapper mapper;

    @Value("${serpapi.api-key}")
    private String apiKey;

    public SerpApiHotelClient(
            WebClient.Builder builder,
            @Value("${serpapi.base-url}") String baseUrl,
            SerpApiHotelMapper mapper) {

        this.webClient =
                builder
                        .baseUrl(baseUrl)
                        .build();

        this.mapper = mapper;
    }

    public Mono<List<HotelOption>> searchHotels(
            TravelSearchRequest request) {

        return webClient.get()

                .uri(uri -> uri

                        .path("/search")

                        .queryParam("engine", "google_hotels")

                        .queryParam("q", request.destination())

                        .queryParam(
                                "check_in_date",
                                request.departureDate()
                        )

                        .queryParam(
                                "check_out_date",
                                request.returnDate()
                        )

                        .queryParam(
                                "adults",
                                request.adults()
                        )

                        .queryParam(
                                "currency",
                                "INR"
                        )

                        .queryParam(
                                "api_key",
                                apiKey
                        )

                        .build())

                .retrieve()

                .bodyToMono(JsonNode.class)

                .doOnNext(json ->
                        log.info(
                                "SerpAPI raw hotel response:\n{}",
                                json.toPrettyString()
                        ))

                .map(json ->
                        mapper.map(
                                json,
                                request.destination()
                        ))

                .doOnSuccess(list ->
                        log.info(
                                "Found {} hotels",
                                list.size()
                        ))

                .onErrorResume(ex -> {

                    log.error(
                            "Hotel Search Failed",
                            ex
                    );

                    return Mono.just(List.of());
                });

    }

    public Mono<HotelOption> revalidateHotel(
            HotelRevalidationRequest request
    ) {
        TravelSearchRequest searchRequest =
                new TravelSearchRequest(
                        TravelType.HOTEL,
                        null,
                        request.city(),
                        request.checkIn(),
                        request.checkOut(),
                        request.adults(),
                        0,
                        request.rooms()
                );

        return searchHotels(searchRequest)

                .flatMapMany(reactor.core.publisher.Flux::fromIterable)

                .filter(hotel ->

                        hotel.hotelId() != null
                                && hotel.hotelId()
                                .equals(request.hotelId())

                                ||

                                hotel.hotelName()
                                        .equalsIgnoreCase(
                                                request.hotelName()
                                        )
                )

                .next();
    }


}
