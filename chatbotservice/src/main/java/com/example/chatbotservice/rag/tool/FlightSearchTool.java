//package com.example.chatbotservice.rag.tool;
//
//
//import com.example.chatbotservice.airport.dto.AirportDTO;
//import com.example.chatbotservice.airport.service.AirportResolverService;
//import com.example.chatbotservice.rag.planner.IntentType;
//
//
//import com.example.chatbotservice.rag.planner.service.TravelPlan;
//import com.example.chatbotservice.service.ToolExecutorService;
//import org.springframework.stereotype.Component;
//
//import java.util.Map;
//import java.util.Optional;
//
//@Component
//public class FlightSearchTool implements TravelTool{
//
//    private final AirportResolverService airportResolver;
//    private final ToolExecutorService executor;
//
//    public FlightSearchTool(
//            AirportResolverService airportResolver,
//            ToolExecutorService executor) {
//
//        this.airportResolver = airportResolver;
//        this.executor = executor;
//    }
//
//
//    @Override
//    public ToolType type() {
//        return ToolType.FLIGHT_SEARCH;
//    }
//
//    @Override
//    public boolean supports(TravelPlan plan) {
//        return plan != null
//                && plan.intent() == IntentType.FLIGHT_SEARCH;
//    }
//
//    @Override
//    public ToolResult execute(String userId, String conversationId, TravelPlan plan) {
//        if (plan.origin() == null
//                || plan.destination() == null
//                || plan.travelDate() == null) {
//
//            return ToolResult.failure(
//                    ToolType.FLIGHT_SEARCH,
//                    "Please provide origin, destination and travel date."
//            );
//        }
//
//        var origin =
//                airportResolver.resolve(plan.origin());
//
//        var destination =
//                airportResolver.resolve(plan.destination());
//
//        if (origin.isEmpty()) {
//
//            return ToolResult.failure(
//                    ToolType.FLIGHT_SEARCH,
//                    "I could not find an airport for "
//                            + plan.origin()
//            );
//        }
//
//        if (destination.isEmpty()) {
//
//            return ToolResult.failure(
//                    ToolType.FLIGHT_SEARCH,
//                    "I could not find an airport for "
//                            + plan.destination()
//            );
//        }
//
//        AirportDTO originAirport = origin.get();
//        AirportDTO destinationAirport = destination.get();
//
//        String result =
//                executor.execute(
//                        "search_flights",
//                        Map.of(
//                                "origin", originAirport.iataCode(),
//                                "destination", destinationAirport.iataCode(),
//                                "date", plan.travelDate()
//                        ),
//                        userId
//                );
//
//        System.out.println("========== FLIGHT TOOL RESULT ==========");
//        System.out.println(result);
//        System.out.println("=========================================");
//
//
//
//
//        return ToolResult.success(
//                ToolType.FLIGHT_SEARCH,
//                result,
//                Map.of(
//                        "origin",
//                        originAirport.iataCode(),
//
//                        "destination",
//                        destinationAirport.iataCode(),
//
//                        "date",
//                        plan.travelDate()
//                )
//        );
//    }
//}
