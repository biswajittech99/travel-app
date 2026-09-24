package com.example.chatbotservice.rag.planner.service;

import com.example.chatbotservice.rag.planner.IntentType;

public record PlannerResponse(boolean success,

                              TravelPlan plan,

                              String error) {
    public static PlannerResponse success(
            TravelPlan plan) {

        return new PlannerResponse(
                true,
                plan,
                null
        );
    }
    public static PlannerResponse failure(
            String error,
            String question) {

        return new PlannerResponse(
                false,
                new TravelPlan(
                        IntentType.GENERAL_CHAT,
                        null,       // origin
                        null,       // destination
                        null,       // travelDate
                        null,       // returnDate
                        1,          // adults
                        0,          // children
                        null,       // rooms
                        null,       // hotelName
                        null,       // bookingId
                        question    // userQuestion
                ),
                error
        );
    }
}
