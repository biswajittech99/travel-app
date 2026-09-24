package com.example.chatbotservice.rag.planner.service;

import com.example.chatbotservice.rag.planner.IntentType;

import java.time.LocalDate;

public record TravelPlan(
        IntentType intent,

        String origin,

        String destination,

        LocalDate travelDate,

        LocalDate returnDate,

        Integer adults,

        Integer children,

        Integer rooms,

        String hotelName,

        String bookingId,

        String userQuestion)
{

}

