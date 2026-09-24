//package com.example.chatbotservice.rag.planner.service;
//
//import com.example.chatbotservice.rag.planner.IntentType;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.ai.chat.client.ChatClient;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDate;
//
//@Service
//public class PlannerService {
//
//    private final ChatClient chatClient;
//    private final ObjectMapper objectMapper;
//
//    public PlannerService(
//            ChatClient.Builder chatClientBuilder,
//            ObjectMapper objectMapper) {
//
//        this.chatClient = chatClientBuilder.build();
//        this.objectMapper = objectMapper;
//    }
//
//    public PlannerResponse createPlan(
//            String question) {
//
//        try {
//
//            String prompt = buildPrompt(question);
//
//            String response =
//                    chatClient.prompt()
//                            .user(prompt)
//                            .call()
//                            .content();
//
//            TravelPlan plan =
//                    objectMapper.readValue(
//                            response,
//                            TravelPlan.class
//                    );
//
//            return PlannerResponse.success(normalize (plan, question)
//            );
//
//        } catch (Exception ex) {
//
//            return PlannerResponse.failure(
//                    ex.getMessage(),
//                    question
//            );
//        }
//    }
//
//    private TravelPlan normalize(
//            TravelPlan plan,
//            String question) {
//
//        return new TravelPlan(
//
//                plan.intent() != null
//                        ? plan.intent()
//                        : IntentType.GENERAL_CHAT,
//
//                normalizeString(plan.origin()),
//
//                normalizeString(plan.destination()),
//
//                normalizeString(plan.travelDate()),
//
//
//
//                plan.adults() != null
//                        ? plan.adults()
//                        : 1,
//                plan.children() != null
//                        ? plan.children()
//                        : 0,
//
//                plan.rooms() != null
//                        ? plan.rooms()
//                        : 1,
//
//                normalizeString(plan.hotelName()),
//
//                normalizeString(plan.bookingId()),
//
//                question
//        );
//    }
//
//    private String normalizeString(
//            String value) {
//
//        if (value == null) {
//            return null;
//        }
//
//        String trimmed = value.trim();
//
//        return trimmed.isEmpty()
//                ? null
//                : trimmed;
//    }
//
//    private String buildPrompt(
//            String question) {
//
//        return """
//You are a travel intent planner.
//
//Your job is to convert the user request into JSON.
//
//Current date: %s
//
//Supported intents:
//
//FLIGHT_SEARCH
//HOTEL_SEARCH
//HOTEL_BOOK
//HOTEL_CANCEL
//FLIGHT_STATUS
//PRICE_REVALIDATION
//GENERAL_CHAT
//
//Return ONLY valid JSON.
//
//Do not add markdown.
//Do not add explanations.
//Do not add ```json.
//
//JSON format:
//
//{
//  "intent": "FLIGHT_SEARCH",
//  "origin": null,
//  "destination": null,
//  "travelDate": null,
//  "returnDate": null,
//  "adults": 1,
//  "children": 0,
//  "rooms": 1,
//  "hotelName": null,
//  "bookingId": null,
//  "userQuestion": ""
//}
//
//Rules:
//
//1. Convert relative dates like tomorrow or next Friday
//   into ISO format YYYY-MM-DD.
//
//2. Do not invent origin or destination.
//
//3. Preserve city names exactly.
//
//4. For flight searches:
//   origin and destination are important.
//
//5. For hotel searches:
//   destination is the hotel city.
//
//6. If the user asks to book a hotel,
//   use HOTEL_BOOK.
//
//7. If the user asks about an existing booking,
//   use bookingId when available.
//
//8. If the request is not travel related,
//   use GENERAL_CHAT.
//
//User request:
//
//%s
//""".formatted(
//                LocalDate.now(),
//                question
//        );
//    }
//}
