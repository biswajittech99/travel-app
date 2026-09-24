package com.example.searchservice.controller;


import com.example.searchservice.dto.HotelOption;
import com.example.searchservice.dto.TravelOption;
import com.example.searchservice.dto.TravelSearchResponse;
import com.example.searchservice.enums.TravelType;
import com.example.searchservice.request.TravelSearchRequest;
import com.example.searchservice.service.InventoryProducer;
import com.example.searchservice.service.SearchAggregatorService;
import org.example.common.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/search")
public class RealtimeSearchController {

    private final SearchAggregatorService searchAggregatorService;
    private final InventoryProducer inventoryProducer;
    private static final Logger log =
            LoggerFactory.getLogger(RealtimeSearchController.class);

    public RealtimeSearchController(
            SearchAggregatorService searchAggregatorService,
            InventoryProducer inventoryProducer) {
        this.searchAggregatorService = searchAggregatorService;
        this.inventoryProducer = inventoryProducer;
    }

    // ✅ FIX 1: GET endpoint for AI chatbot compatibility
    @GetMapping("/flights")
    public Mono<List<TravelOption>> searchFlights(
            @RequestParam String origin,

            @RequestParam String destination,

            @RequestParam String date) {

        TravelSearchRequest request =
                new TravelSearchRequest(

                        TravelType.FLIGHT,

                        origin,

                        destination,

                        LocalDate.parse(date),

                        null,

                        1,

                        0,

                        null

                );

        return searchAggregatorService.searchFlights(request);
    }

    @PostMapping("/flights/revalidate")
    public Mono<FlightRevalidationResponse> revalidateFlight(
            @RequestBody FlightRevalidationRequest request
    ) {
        log.info(
                "Received flight  revalidation request: {}",
                request.flightId()
        );


        return searchAggregatorService
                .revalidateFlight(request);
    }

    @PostMapping("/hotels/revalidate")
    public Mono<HotelRevalidationResponse> revalidateHotel(
            @RequestBody HotelRevalidationRequest request
    ) {

        log.info(
                "Received hotel revalidation request: {}",
                request.hotelId()
        );

        return searchAggregatorService
                .revalidateHotel(request);
    }

    // ✅ Keep POST for frontend/direct use
    @PostMapping("/flights")
    public Mono<List<TravelOption>> searchFlightsPost(
            @RequestBody TravelSearchRequest request) {
        return searchAggregatorService.searchFlights(request);
    }

    // ✅ Manual inventory publish
    @PostMapping("/inventory")
    public ResponseEntity<String> publishInventory(
            @RequestBody FlightInventoryDTO dto) {
        inventoryProducer.publishFlight(dto);
        return ResponseEntity.ok(
                "Inventory sent to Kafka: " + dto.getFlightId());
    }

    // ✅ FIX 2: Return Mono instead of blocking
    @GetMapping("/{flightId}/price")
    public Mono<BigDecimal> getLivePrice(
            @PathVariable String flightId) {
        return searchAggregatorService.getLivePrice(flightId);
    }

    @PostMapping
    public Mono<TravelSearchResponse> search(
            @RequestBody TravelSearchRequest request) {

        return searchAggregatorService.search(request);


    }

    @GetMapping("/hotels")
    public Mono<List<HotelOption>> searchHotels(

            @RequestParam String city,

            @RequestParam String checkIn,

            @RequestParam String checkOut,

            @RequestParam(defaultValue = "1") Integer adults,

            @RequestParam(defaultValue = "1") Integer rooms) {

        TravelSearchRequest request =
                new TravelSearchRequest(

                        TravelType.HOTEL,

                        null,

                        city,

                        LocalDate.parse(checkIn),

                        LocalDate.parse(checkOut),

                        adults,

                        0,

                        rooms
                );

        return searchAggregatorService
                .searchHotels(request);
    }

    @PostMapping("/hotels")
    public Mono<List<HotelOption>> searchHotels(
            @RequestBody TravelSearchRequest request) {

        return searchAggregatorService.searchHotels(request);
    }







}
