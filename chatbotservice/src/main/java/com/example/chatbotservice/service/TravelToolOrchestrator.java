package com.example.chatbotservice.service;

import com.example.chatbotservice.airport.dto.AirportDTO;
import com.example.chatbotservice.airport.service.AirportResolverService;
import com.example.chatbotservice.rag.planner.service.TravelPlan;
import com.example.chatbotservice.rag.tool.ToolResult;
import com.example.chatbotservice.rag.tool.ToolType;
import com.example.chatbotservice.rag.tool.TravelTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
public class TravelToolOrchestrator {

    private static final Logger log =
            LoggerFactory.getLogger(TravelToolOrchestrator.class);

    private final AirportResolverService airportResolverService;
    private final ToolExecutorService toolExecutorService;

    public TravelToolOrchestrator(
            AirportResolverService airportResolverService,
            ToolExecutorService toolExecutorService) {

        this.airportResolverService = airportResolverService;
        this.toolExecutorService = toolExecutorService;
    }

    public ToolResult execute(
            String userId,
            String conversationId,
            TravelPlan plan) {

        System.out.println("========== TOOL ORCHESTRATOR ==========");
        System.out.println("Intent: " + plan.intent());
        System.out.println("Origin: " + plan.origin());
        System.out.println("Destination: " + plan.destination());
        System.out.println("Date: " + plan.travelDate());


        log.info(
                "Tool orchestrator received intent: {}",
                plan.intent()
        );

        return switch (plan.intent()) {

            case FLIGHT_SEARCH -> {

                log.info("Executing FLIGHT_SEARCH tool");

                ToolResult result =
                        executeFlightSearch(
                                userId,
                                conversationId,
                                plan
                        );

                log.info(
                        "FLIGHT_SEARCH completed. success={}, message={}",
                        result.success(),
                        result.message()
                );

                yield result;
            }
            case HOTEL_SEARCH -> {

                log.info("Executing HOTEL_SEARCH tool");

                ToolResult result =
                        executeHotelSearch(
                                userId,
                                conversationId,
                                plan
                        );

                log.info(
                        "HOTEL_SEARCH completed. success={}, message={}",
                        result.success(),
                        result.message()
                );

                yield result;
            }

            default -> {

                log.info(
                        "No live tool required for intent: {}",
                        plan.intent()
                );

                yield ToolResult.success(
                        ToolType.FLIGHT_SEARCH,
                        "No live tool required.",
                        Map.of()
                );
            }
        };
    }

    private ToolResult executeFlightSearch(
            String userId,
            String conversationId,
            TravelPlan plan) {

        log.info(
                "Executing FLIGHT_SEARCH: {} -> {} on {}",
                plan.origin(),
                plan.destination(),
                plan.travelDate()
        );

        // ---------------------------------------
        // Validate required fields BEFORE calling any
        // downstream service — a missing date/city here
        // must never reach a .toString() on a null LocalDate.
        // ---------------------------------------

        if (plan.origin() == null || plan.origin().isBlank()
                || plan.destination() == null || plan.destination().isBlank()) {

            return ToolResult.failure(
                    ToolType.FLIGHT_SEARCH,
                    "I need both an origin and a destination city to search flights. Could you tell me where you're flying from and to?"
            );
        }

        if (plan.travelDate() == null) {

            return ToolResult.failure(
                    ToolType.FLIGHT_SEARCH,
                    "I need a travel date to search flights. Which date would you like to fly?"
            );
        }

        // ---------------------------------------
        // Resolve origin airport
        // ---------------------------------------

        Optional<AirportDTO> origin =
                airportResolverService.resolve(
                        plan.origin()
                );

        // ---------------------------------------
        // Resolve destination airport
        // ---------------------------------------

        Optional<AirportDTO> destination =
                airportResolverService.resolve(
                        plan.destination()
                );

        if (origin.isEmpty() || destination.isEmpty()) {

            log.error(
                    "Could not resolve airports: {} -> {}",
                    plan.origin(),
                    plan.destination()
            );

            return ToolResult.failure(
                    ToolType.FLIGHT_SEARCH,
                    "Could not resolve origin or destination airport."
            );
        }

        String originIata =
                origin.get().iataCode();

        String destinationIata =
                destination.get().iataCode();

        log.info(
                "Resolved airports: {} -> {}",
                originIata,
                destinationIata
        );

        // ---------------------------------------
        // Build tool input
        // ---------------------------------------

        Map<String, Object> input =
                Map.of(
                        "origin", originIata,
                        "destination", destinationIata,
                        "date", plan.travelDate().toString()
                );

        log.info(
                "Calling ToolExecutorService search_flights with: {}",
                input
        );

        // ---------------------------------------
        // Execute search service call
        // ---------------------------------------

        String result =
                toolExecutorService.execute(
                        "search_flights",
                        input,
                        userId
                );

        log.info(
                "Flight tool returned: {}",
                result
        );

        if (result == null || result.isBlank()) {

            return ToolResult.failure(
                    ToolType.FLIGHT_SEARCH,
                    "No flight search result returned."
            );
        }

        return ToolResult.success(
                ToolType.FLIGHT_SEARCH,
                result,
                Map.of()
        );
    }

    private ToolResult executeHotelSearch(
            String userId,
            String conversationId,
            TravelPlan plan) {

        log.info(
                "Executing HOTEL_SEARCH: {} from {} to {}",
                plan.destination(),
                plan.travelDate(),
                plan.returnDate()
        );

        // ---------------------------------------
        // Validate required fields BEFORE calling any
        // downstream service — this is exactly what was
        // crashing with a NullPointerException before.
        // ---------------------------------------

        if (plan.destination() == null || plan.destination().isBlank()) {

            return ToolResult.failure(
                    ToolType.HOTEL_SEARCH,
                    "Which city would you like to find hotels in?"
            );
        }

        if (plan.travelDate() == null || plan.returnDate() == null) {

            return ToolResult.failure(
                    ToolType.HOTEL_SEARCH,
                    "I need both a check-in and check-out date to search hotels. Could you share your travel dates?"
            );
        }

        Map<String, Object> input =
                Map.of(
                        "city",
                        plan.destination(),

                        "checkIn",
                        plan.travelDate().toString(),

                        "checkOut",
                        plan.returnDate().toString(),

                        "adults",
                        plan.adults(),

                        "rooms",
                        plan.rooms()
                );

        log.info(
                "Calling ToolExecutorService search_hotels with: {}",
                input
        );

        String result =
                toolExecutorService.execute(
                        "search_hotels",
                        input,
                        userId
                );

        log.info(
                "Hotel tool returned: {}",
                result
        );

        if (result == null ||
                result.isBlank()) {

            return ToolResult.failure(
                    ToolType.HOTEL_SEARCH,
                    "No hotel search result returned."
            );
        }

        if (result.contains("\"error\"")) {

            return ToolResult.failure(
                    ToolType.HOTEL_SEARCH,
                    "Hotel search failed."
            );
        }

        return ToolResult.success(
                ToolType.HOTEL_SEARCH,
                result,
                Map.of(
                        "city",
                        plan.destination(),

                        "checkIn",
                        plan.travelDate().toString(),

                        "checkOut",
                        plan.returnDate().toString()
                )
        );
    }
}