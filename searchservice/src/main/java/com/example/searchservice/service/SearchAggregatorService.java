package com.example.searchservice.service;

import com.example.searchservice.dto.HotelOption;
import com.example.searchservice.dto.TravelOption;
import com.example.searchservice.dto.TravelSearchResponse;
import com.example.searchservice.enums.TravelType;
import com.example.searchservice.request.TravelSearchRequest;

import com.example.searchservice.supplier.AviationStackFlightClient;
import com.example.searchservice.supplier.SerpApiHotelClient;
import com.example.searchservice.supplier.SupplierClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.common.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class SearchAggregatorService {
    private final List<SupplierClient> suppliers;
    private final AviationStackFlightClient flightClient;
    private final SerpApiHotelClient serpApiHotelClient;
    private final InventoryProducer producer;
    private final ReactiveRedisTemplate<String, String> redis; // explicitly typed
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private static final Logger log =
            LoggerFactory.getLogger(SearchAggregatorService.class);

    public SearchAggregatorService(List<com.example.searchservice.supplier.SupplierClient> suppliers, AviationStackFlightClient flightClient,
                                   InventoryProducer producer,
                                   ReactiveRedisTemplate<String, String> redis, SerpApiHotelClient serpApiHotelClient) {
        this.suppliers = suppliers;
        this.flightClient = flightClient;

        this.producer = producer;
        this.redis = redis;
        this.serpApiHotelClient = serpApiHotelClient;
    }

    public Mono<List<TravelOption>> searchFlights(
            TravelSearchRequest request) {

        return Flux.fromIterable(suppliers)
                .filter(supplier ->
                        supplier.supportedType()
                                == TravelType.FLIGHT)

                .flatMap(supplier -> {

                    log.info(
                            "Calling flight supplier: {}",
                            supplier.getClass().getSimpleName()
                    );

                    return supplier.search(request)
                            .doOnNext(results ->
                                    log.info(
                                            "Supplier {} returned {} flights",
                                            supplier.getClass().getSimpleName(),
                                            results.size()
                                    )
                            )
                            .doOnError(error ->
                                    log.error(
                                            "Flight supplier {} failed",
                                            supplier.getClass().getSimpleName(),
                                            error
                                    )
                            )
                            .onErrorResume(error ->
                                    Mono.just(List.of())
                            );
                })

                .flatMapIterable(list -> list)

                .collectList()

                .doOnNext(list ->
                        log.info(
                                "Total flights returned by SearchAggregator: {}",
                                list.size()
                        )
                );

    }

    public Mono<BigDecimal> getLivePrice(String flightIdOrSearchKey) {
        String redisKey = "inventory:flight:" + flightIdOrSearchKey;

        return redis.opsForValue()
                .get(redisKey)
                .flatMap(json -> {
                    try {
                        FlightInventoryDTO dto =
                                objectMapper.readValue(json, FlightInventoryDTO.class);

                        return Mono.just(dto.getPrice());   // Already BigDecimal
                    } catch (Exception e) {
                        return Mono.error(e);
                    }
                })
                .switchIfEmpty(Mono.error(new RuntimeException("No inventory found in Redis")));
    }
    private FlightInventoryDTO mapToDto(TravelOption opt) {
        FlightInventoryDTO dto = new FlightInventoryDTO();

        // ✅ Use flightNumber in ID if available
        String flightId = (opt.flightNumber() != null &&
                !opt.flightNumber().isEmpty())
                ? opt.supplier() + ":" + opt.flightNumber()
                : opt.supplier() + ":" + opt.id();
        // Result: "AI:AI302" or fallback "AI:1"

        dto.setFlightId(flightId);
        dto.setSupplier(opt.supplier());
        dto.setFromAirport(opt.from());
        dto.setToAirport(opt.to());
        dto.setDeparture(LocalDateTime.ofInstant(
                opt.departure(), ZoneOffset.UTC));
        dto.setArrival(LocalDateTime.ofInstant(
                opt.arrival(), ZoneOffset.UTC));
        dto.setSeatsAvailable(opt.seatsAvailable());
        dto.setPrice(opt.price());
        dto.setCurrency(
                opt.currency() == null || opt.currency().isBlank()
                        ? "UNKNOWN"
                        : opt.currency()
        );
        dto.setSupplierOfferId(opt.id());
        dto.setLastSyncedAt(LocalDateTime.now());
        return dto;
    }

    public Mono<List<HotelOption>> searchHotels(
            TravelSearchRequest request) {

        log.info(
                "Starting hotel search: city={}, checkIn={}, checkOut={}, adults={}, rooms={}",
                request.destination(),
                request.departureDate(),
                request.returnDate(),
                request.adults(),
                request.rooms()
        );

        return serpApiHotelClient
                .searchHotels(request)

                .doOnNext(results ->
                        log.info(
                                "SerpApi returned {} hotels",
                                results.size()
                        )
                )

                .doOnError(error ->
                        log.error(
                                "SerpApi hotel search failed",
                                error
                        )
                )

                .onErrorResume(error ->
                        Mono.just(List.of())
                );
    }

    public Mono<TravelSearchResponse> search(
            TravelSearchRequest request) {

        Mono<List<TravelOption>> flights =
                request.travelType() == TravelType.FLIGHT
                        ? searchFlights(request)
                        : Mono.just(List.of());

        Mono<List<HotelOption>> hotels =
                request.travelType() == TravelType.HOTEL
                        ? searchHotels(request)
                        : Mono.just(List.of());

        return Mono.zip(flights, hotels)

                .map(tuple -> new TravelSearchResponse(

                        tuple.getT1(),

                        tuple.getT2()

                ));

    }

    public Mono<FlightRevalidationResponse> revalidateFlight(
            FlightRevalidationRequest request
    ) {

        return flightClient
                .revalidate(request)

                .doOnNext(response ->
                        System.out.println(
                                "Flight revalidation completed: " +
                                        response
                        )
                )

                .onErrorResume(error -> {

                    return Mono.just(
                            new FlightRevalidationResponse(

                                    false,

                                    false,

                                    request.originalPrice(),

                                    null,

                                    request.currency(),

                                    request.flightNumber(),

                                    "Flight revalidation failed: "
                                            + error.getMessage(),

                                    java.time.Instant.now()
                            )
                    );
                });
    }

    public Mono<HotelRevalidationResponse> revalidateHotel(
            HotelRevalidationRequest request
    ) {

        return serpApiHotelClient
                .revalidateHotel(request)

                .map(hotel -> {

                    BigDecimal currentPrice =
                            hotel.price();

                    boolean available =
                            currentPrice != null
                                    && currentPrice.compareTo(
                                    BigDecimal.ZERO
                            ) > 0;

                    if (!available) {

                        return new HotelRevalidationResponse(
                                false,                         // available
                                false,                         // priceChanged
                                request.originalPrice(),       // originalPrice
                                null,                          // currentPrice
                                request.hotelId(),             // hotelId
                                request.hotelName(),           // hotelName
                                request.checkIn(),             // checkIn
                                request.checkOut(),            // checkOut
                                request.currency(),            // currency
                                0,                             // availableRooms
                                "Hotel is currently unavailable"
                        );
                    }

                    boolean priceChanged =

                            request.originalPrice() != null

                                    && currentPrice.compareTo(
                                    request.originalPrice()
                            ) != 0;

                    String message;

                    if (priceChanged) {

                        message =
                                "Hotel price has changed. "
                                        + "Fresh price: "
                                        + currentPrice;

                    } else {

                        message =
                                "Hotel is available at the latest price";
                    }

                    return new HotelRevalidationResponse(
                            true,                          // available
                            priceChanged,                  // priceChanged
                            request.originalPrice(),       // originalPrice
                            currentPrice,                  // currentPrice
                            hotel.hotelId(),                    // hotelId
                            hotel.hotelName(),             // hotelName
                            request.checkIn(),             // checkIn
                            request.checkOut(),            // checkOut
                            hotel.currency(),              // currency
                            hotel.availableRooms(),        // availableRooms
                            message                        // message
                    );
                })

                .switchIfEmpty(

                        Mono.just(

                                new HotelRevalidationResponse(
                                        false,                         // available
                                        false,                         // priceChanged
                                        request.originalPrice(),       // originalPrice
                                        null,                          // currentPrice
                                        request.hotelId(),             // hotelId
                                        request.hotelName(),           // hotelName
                                        request.checkIn(),             // checkIn
                                        request.checkOut(),            // checkOut
                                        request.currency(),            // currency
                                        0,                             // availableRooms
                                        "Hotel is currently unavailable"
                                )
                        )
                )

                .onErrorResume(ex -> {

                    log.error(
                            "Hotel revalidation failed",
                            ex
                    );

                    return Mono.just(

                            new HotelRevalidationResponse(
                                    false,
                                    false,
                                    request.originalPrice(),
                                    null,
                                    request.hotelId(),
                                    request.hotelName(),
                                    request.checkIn(),
                                    request.checkOut(),
                                    request.currency(),
                                    0,
                                    "Hotel revalidation failed"
                            )
                    );
                });
    }





}