package com.example.chatbotservice.rag.tool;

import com.example.chatbotservice.rag.planner.IntentType;
import com.example.chatbotservice.rag.planner.service.TravelPlan;
import com.example.chatbotservice.service.ToolExecutorService;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class HotelSearchTool implements TravelTool{

    private final ToolExecutorService executor;

    public HotelSearchTool(
            ToolExecutorService executor) {

        this.executor = executor;
    }

    @Override
    public ToolType type() {
        return ToolType.HOTEL_SEARCH;
    }

    @Override
    public boolean supports(TravelPlan plan) {
        return plan != null
                && plan.intent() == IntentType.HOTEL_SEARCH;
    }

    @Override
    public ToolResult execute(String userId, String conversationId, TravelPlan plan) {
        // =====================================================
        // VALIDATE DESTINATION
        // =====================================================

        if (plan.destination() == null
                || plan.destination().isBlank()) {

            return ToolResult.failure(
                    ToolType.HOTEL_SEARCH,
                    "Please provide the hotel destination."
            );
        }


        // =====================================================
        // VALIDATE CHECK-IN DATE
        // =====================================================

        if (plan.travelDate() == null) {

            return ToolResult.failure(
                    ToolType.HOTEL_SEARCH,
                    "Please provide your check-in date."
            );
        }


        // =====================================================
        // VALIDATE CHECK-OUT DATE
        // =====================================================

        if (plan.returnDate() == null) {

            return ToolResult.failure(
                    ToolType.HOTEL_SEARCH,
                    "Please provide your check-out date."
            );
        }


        // =====================================================
        // BUILD TOOL INPUT
        // =====================================================

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


        // =====================================================
        // EXECUTE HOTEL SEARCH
        // =====================================================

        String result =
                executor.execute(

                        "search_hotels",

                        input,

                        userId
                );


        // =====================================================
        // VALIDATE RESPONSE
        // =====================================================

        if (result == null
                || result.isBlank()) {

            return ToolResult.failure(

                    ToolType.HOTEL_SEARCH,

                    "No live hotel results were returned."
            );
        }


        if (result.contains("\"error\"")) {

            return ToolResult.failure(

                    ToolType.HOTEL_SEARCH,

                    "Hotel search failed."
            );
        }


        // =====================================================
        // SUCCESS
        // =====================================================

        return ToolResult.success(

                ToolType.HOTEL_SEARCH,

                result,

                Map.of(

                        "city",
                        plan.destination(),

                        "checkIn",
                        plan.travelDate(),

                        "checkOut",
                        plan.returnDate()
                )
        );
    }
}
