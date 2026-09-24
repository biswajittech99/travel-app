//package com.example.chatbotservice.rag.tool;
//
//import com.example.chatbotservice.rag.planner.IntentType;
//import com.example.chatbotservice.rag.planner.service.TravelPlan;
//import com.example.chatbotservice.service.ToolExecutorService;
//import org.springframework.stereotype.Component;
//
//import java.util.Map;
//
//@Component
//public class FlightRevalidationTool implements TravelTool{
//
//    private final ToolExecutorService executor;
//
//    public FlightRevalidationTool(
//            ToolExecutorService executor
//    ) {
//        this.executor = executor;
//    }
//    @Override
//    public ToolType type() {
//        return ToolType.FLIGHT_REVALIDATION;
//    }
//
//    @Override
//    public boolean supports(TravelPlan plan) {
//        return plan != null
//                && plan.intent()
//                == IntentType.FLIGHT_REVALIDATION;
//    }
//
//    @Override
//    public ToolResult execute(String userId, String conversationId, TravelPlan plan) {
//        if (plan.selectedFlightId() == null) {
//
//            return ToolResult.failure(
//                    ToolType.FLIGHT_REVALIDATION,
//                    "No flight was selected for revalidation."
//            );
//        }
//
//        String result =
//                executor.execute(
//                        "revalidate_flight",
//                        Map.of(
//                                "flight_id",
//                                plan.selectedFlightId(),
//                                "origin",
//                                plan.origin(),
//                                "destination",
//                                plan.destination(),
//                                "date",
//                                plan.travelDate(),
//                                "adults",
//                                plan.adults()
//                        ),
//                        userId
//                );
//
//        if (result == null ||
//                result.isBlank() ||
//                result.contains("\"available\":false")) {
//
//            return ToolResult.failure(
//                    ToolType.FLIGHT_REVALIDATION,
//                    "The selected flight is no longer available."
//            );
//        }
//
//        return ToolResult.success(
//                ToolType.FLIGHT_REVALIDATION,
//                result,
//                Map.of(
//                        "flightId",
//                        plan.selectedFlightId()
//                )
//        );
//
//    }
//}
