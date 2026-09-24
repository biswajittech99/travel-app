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
//public class HotelRevalidationTool  implements TravelTool{
//
//    private final ToolExecutorService executor;
//
//    public HotelRevalidationTool(
//            ToolExecutorService executor
//    ) {
//        this.executor = executor;
//    }
//
//    @Override
//    public ToolType type() {
//        return ToolType.HOTEL_REVALIDATION;
//    }
//
//    @Override
//    public boolean supports(TravelPlan plan) {
//        return plan != null
//                && plan.intent()
//                == IntentType.HOTEL_REVALIDATION;
//    }
//
//    @Override
//    public ToolResult execute(String userId, String conversationId, TravelPlan plan) {
//        if (plan.selectedHotelId() == null) {
//
//            return ToolResult.failure(
//                    ToolType.HOTEL_REVALIDATION,
//                    "No hotel was selected for revalidation."
//            );
//        }
//
//        String result =
//                executor.execute(
//                        "revalidate_hotel",
//                        Map.of(
//                                "hotel_id",
//                                plan.selectedHotelId(),
//                                "city",
//                                plan.destination(),
//                                "check_in",
//                                plan.checkIn(),
//                                "check_out",
//                                plan.checkOut(),
//                                "adults",
//                                plan.adults(),
//                                "rooms",
//                                plan.rooms()
//                        ),
//                        userId
//                );
//
//        return ToolResult.success(
//                ToolType.HOTEL_REVALIDATION,
//                result,
//                Map.of(
//                        "hotelId",
//                        plan.selectedHotelId()
//                )
//        );
//
//    }
//}
