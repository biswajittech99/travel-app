package com.example.searchservice.supplier;

import com.example.searchservice.dto.SearchRequest;
import com.example.searchservice.dto.TravelOption;
import com.example.searchservice.enums.TravelType;
import com.example.searchservice.request.TravelSearchRequest;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.common.dto.FlightRevalidationRequest;
import org.example.common.dto.FlightRevalidationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Component

public class AviationStackFlightClient implements SupplierClient{

    private final WebClient webClient;
   // private final AmadeusAuthService authService;
    private final Logger log = LoggerFactory.getLogger(AviationStackFlightClient.class);

    @Value("${aviationstack.api-key}")
    private String apiKey;

    public AviationStackFlightClient(
            WebClient.Builder builder,
            @Value("${aviationstack.base-url}")
            String baseUrl) {

        this.webClient =
                builder
                        .baseUrl(baseUrl)
                        .build();
    }

    private Mono<List<TravelOption>> searchFlights(SearchRequest req) {
        if (req.date().isBefore(LocalDate.now())) {
            log.warn("⚠️ Past date rejected: {}", req.date());
            return Mono.just(List.of());
        }
        if (req.from().length() != 3 || req.to().length() != 3) {
            log.warn("⚠️ Invalid airport codes: {} {}",
                    req.from(), req.to());
            return Mono.just(List.of());
        }
        log.info(
                "Calling AviationStack: {} -> {} on {}",
                req.from(),
                req.to(),
                req.date()
        );


        return webClient.get()
                .uri(uri -> uri
                        .path("/flights")
                        .queryParam("access_key", apiKey)
                        .queryParam("dep_iata", req.from())
                        .queryParam("arr_iata", req.to())
                        .queryParam("flight_date", req.date())
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .doOnNext(json -> {
                    log.info(
                            "============== AVIATIONSTACK RESPONSE =============="
                    );
                    log.info(json.toPrettyString());
                })
                .map(this::mapAviationStackResponse)
                .doOnError(err ->
                        log.error(
                                "AviationStack request failed: {}",
                                err.getMessage(),
                                err
                        )
                );
    }
    public Mono<List<TravelOption>> fallbackSearch(SearchRequest req, Throwable t) {
        log.warn("fallback for {}->{}: {}", req.from(), req.to(), t.getMessage());
        return Mono.just(List.of());
    }
    private List<TravelOption> mapAviationStackResponse(JsonNode root) {

        List<TravelOption> results = new ArrayList<>();

        if (root == null || !root.has("data")) {
            return results;
        }

        Set<String> seen = new HashSet<>();

        for (JsonNode item : root.get("data")) {

            try {

                String from = item.path("departure")
                        .path("iata")
                        .asText("");

                String to = item.path("arrival")
                        .path("iata")
                        .asText("");

                if (from.isBlank() || to.isBlank()) {
                    continue;
                }

                String depTime =
                        item.path("departure")
                                .path("scheduled")
                                .asText();

                String arrTime =
                        item.path("arrival")
                                .path("scheduled")
                                .asText();

                if (depTime.isBlank() || arrTime.isBlank()) {
                    continue;
                }

                Instant departure =
                        OffsetDateTime.parse(depTime).toInstant();

                Instant arrival =
                        OffsetDateTime.parse(arrTime).toInstant();

                String airline =
                        item.path("airline")
                                .path("name")
                                .asText("Unknown");

                String flightNumber =
                        item.path("flight")
                                .path("iata")
                                .asText("");

                if (flightNumber.isBlank()) {

                    flightNumber =
                            item.path("flight")
                                    .path("number")
                                    .asText(UUID.randomUUID().toString());

                }

                String uniqueKey =
                        flightNumber + departure;

                if (!seen.add(uniqueKey)) {
                    continue;
                }

                results.add(

                        new TravelOption(

                                "AviationStack",

                                UUID.randomUUID().toString(),

                                from,

                                to,

                                flightNumber,

                                "ECONOMY",

                                departure,

                                arrival,

                                BigDecimal.ZERO,

                                "USD",

                                0

                        )

                );

            }

            catch (Exception ex) {

                log.warn("Mapping error : {}", ex.getMessage());

            }

        }

        return results;
    }


    @Override
    public TravelType supportedType() {
        return TravelType.FLIGHT;
    }

    @Override
    public Mono<List<TravelOption>> search(TravelSearchRequest request) {

                SearchRequest req =
                new SearchRequest(
                        request.origin(),
                        request.destination(),
                        request.departureDate());

        return searchFlights(req);
    }
    public Mono<FlightRevalidationResponse> revalidate(
            FlightRevalidationRequest request
    ) {

        log.info(
                "Revalidating flight {} {} -> {} on {}",
                request.flightNumber(),
                request.origin(),
                request.destination(),
                request.departure()
        );

        LocalDate flightDate =
                request.departure()
                        .atZone(ZoneOffset.UTC)
                        .toLocalDate();

        return webClient
                .get()

                .uri(uri -> uri

                        .path("/flights")

                        .queryParam(
                                "access_key",
                                apiKey
                        )

                        .queryParam(
                                "dep_iata",
                                request.origin()
                        )

                        .queryParam(
                                "arr_iata",
                                request.destination()
                        )

                        .queryParam(
                                "flight_date",
                                flightDate
                        )

                        .build()
                )

                .retrieve()

                .bodyToMono(JsonNode.class)

                .map(root ->
                        mapRevalidationResponse(
                                root,
                                request
                        )
                )

                .onErrorResume(error -> {

                    log.error(
                            "Flight revalidation failed for {}",
                            request.flightNumber(),
                            error
                    );

                    return Mono.just(
                            new FlightRevalidationResponse(

                                    false,

                                    false,

                                    request.originalPrice(),

                                    null,

                                    request.currency(),

                                    request.flightNumber(),

                                    "Supplier revalidation failed",

                                    Instant.now()
                            )
                    );
                });
    }

    private FlightRevalidationResponse
    mapRevalidationResponse(

            JsonNode root,

            FlightRevalidationRequest request
    ) {

        if (root == null ||
                !root.has("data") ||
                root.get("data").isEmpty()) {

            return new FlightRevalidationResponse(

                    false,

                    false,

                    request.originalPrice(),

                    null,

                    request.currency(),

                    request.flightNumber(),

                    "Flight is no longer available",

                    Instant.now()
            );
        }

        for (JsonNode item : root.get("data")) {

            String flightNumber =
                    item.path("flight")
                            .path("iata")
                            .asText("");

            if (flightNumber.isBlank()) {

                flightNumber =
                        item.path("flight")
                                .path("number")
                                .asText("");
            }

            String from =
                    item.path("departure")
                            .path("iata")
                            .asText("");

            String to =
                    item.path("arrival")
                            .path("iata")
                            .asText("");

            String departureTime =
                    item.path("departure")
                            .path("scheduled")
                            .asText("");

            // ---------------------------------------------
            // Match flight number
            // ---------------------------------------------

            if (!flightNumber.equalsIgnoreCase(
                    request.flightNumber())) {

                continue;
            }

            // ---------------------------------------------
            // Match origin
            // ---------------------------------------------

            if (!from.equalsIgnoreCase(
                    request.origin())) {

                continue;
            }

            // ---------------------------------------------
            // Match destination
            // ---------------------------------------------

            if (!to.equalsIgnoreCase(
                    request.destination())) {

                continue;
            }

            // ---------------------------------------------
            // Match departure date/time
            // ---------------------------------------------

            if (!departureTime.isBlank()) {

                try {

                    Instant supplierDeparture =
                            OffsetDateTime
                                    .parse(departureTime)
                                    .toInstant();

                    if (!sameTravelDate(
                            supplierDeparture,
                            request.departure())) {

                        continue;
                    }

                } catch (Exception ex) {

                    log.warn(
                            "Could not parse departure time {}",
                            departureTime
                    );
                }
            }

            // =================================================
            // IMPORTANT
            // AVIATIONSTACK DOES NOT PROVIDE LIVE TICKET PRICE
            // =================================================

            BigDecimal currentPrice =
                    request.originalPrice();

            boolean priceChanged =
                    currentPrice != null &&
                            request.originalPrice() != null &&
                            currentPrice.compareTo(
                                    request.originalPrice()
                            ) != 0;

            return new FlightRevalidationResponse(

                    true,

                    priceChanged,

                    request.originalPrice(),

                    currentPrice,

                    request.currency(),

                    request.flightNumber(),

                    "Flight successfully revalidated",

                    Instant.now()
            );
        }

        return new FlightRevalidationResponse(

                false,

                false,

                request.originalPrice(),

                null,

                request.currency(),

                request.flightNumber(),

                "Selected flight is no longer available",

                Instant.now()
        );
    }
    private boolean sameTravelDate(
            Instant first,
            Instant second
    ) {

        LocalDate firstDate =
                first.atZone(
                        ZoneOffset.UTC
                ).toLocalDate();

        LocalDate secondDate =
                second.atZone(
                        ZoneOffset.UTC
                ).toLocalDate();

        return firstDate.equals(secondDate);
    }
}
