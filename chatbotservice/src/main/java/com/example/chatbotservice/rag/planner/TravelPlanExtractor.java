package com.example.chatbotservice.rag.planner;

import com.example.chatbotservice.rag.llm.LlmService;
import com.example.chatbotservice.rag.planner.service.TravelPlan;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class TravelPlanExtractor {
    private final LlmService llmService;
    private final ObjectMapper objectMapper;

    public TravelPlanExtractor(
            LlmService llmService,
            ObjectMapper objectMapper) {

        this.llmService = llmService;
        this.objectMapper = objectMapper;
    }

    public TravelPlan extract(String question) {

        String prompt = """
                You are a travel intent extraction engine.

                Your job is to extract structured travel information
                from the CURRENT user request.

                Current date: %s

                =====================================================
                ALLOWED INTENTS
                =====================================================

                You MUST use exactly one of these values:

                FLIGHT_SEARCH
                HOTEL_SEARCH
                BOOK_FLIGHT
                BOOK_HOTEL
                CANCEL_BOOKING
                FLIGHT_STATUS
                PRICE_REVALIDATION
                KNOWLEDGE
                GENERAL_CHAT

                IMPORTANT:
                Never return any other intent.

                Invalid examples:

                GENERAL_TRAVEL
                TRAVEL
                FLIGHT
                HOTEL
                SEARCH
                HOTEL_BOOK
                HOTEL_CANCEL

                =====================================================
                JSON FORMAT
                =====================================================

                {
                  "intent": "GENERAL_CHAT",
                  "origin": null,
                  "destination": null,
                  "travelDate": null,
                  "returnDate": null,
                  "adults": 1,
                  "children": 0,
                  "rooms": 1,
                  "hotelName": null,
                  "bookingId": null,
                  "userQuestion": null
                }

                =====================================================
                RULES
                =====================================================

                1. Extract information ONLY from the current request.

                2. Do not use values from previous conversations.

                3. Do not invent origin or destination.

                4. Preserve city names from the user's request.

                5. For FLIGHT_SEARCH:

                   origin = departure city
                   destination = arrival city

                6. For HOTEL_SEARCH:

                   destination = hotel city

                7. Convert relative dates into YYYY-MM-DD.

                   Today = %s

                   Tomorrow = today + 1 day.

                8. For example, if today is 2026-08-13:

                   "tomorrow"

                   must become:

                   "2026-08-14"

                9. If adults are not specified:

                   adults = 1

                10. If children are not specified:

                    children = 0

                11. If rooms are not specified:

                    rooms = 1

                12. If the user asks to book a flight:

                    intent = BOOK_FLIGHT

                13. If the user asks to book a hotel:

                    intent = BOOK_HOTEL

                14. If the user asks to cancel a booking:

                    intent = CANCEL_BOOKING

                15. If the user asks about flight status:

                    intent = FLIGHT_STATUS

                16. If the user asks to re-check a flight price:

                    intent = PRICE_REVALIDATION

                17. If the user asks for general travel knowledge:

                    intent = KNOWLEDGE

                18. If the request does not match any travel operation:

                    intent = GENERAL_CHAT

                19. Return ONLY valid JSON.

                20. Do not return markdown.

                21. Do not return explanations.

                22. Do not return ```json.

                =====================================================
                USER REQUEST
                =====================================================

                %s
                """.formatted(
                LocalDate.now(),
                LocalDate.now(),
                question
        );

        try {

            String json = llmService.chat(prompt);

            json = cleanJson(json);

            TravelPlan plan =
                    objectMapper.readValue(
                            json,
                            TravelPlan.class
                    );

            return normalize(plan, question);

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Could not extract travel intent",
                    e
            );
        }
    }

    private TravelPlan normalize(
            TravelPlan plan,
            String question
    ) {

        IntentType intent =
                plan.intent() != null
                        ? plan.intent()
                        : IntentType.GENERAL_CHAT;

        return new TravelPlan(

                intent,

                normalizeString(
                        plan.origin()
                ),

                normalizeString(
                        plan.destination()
                ),

                plan.travelDate(),

                plan.returnDate(),

                plan.adults() != null
                        ? plan.adults()
                        : 1,

                plan.children() != null
                        ? plan.children()
                        : 0,

                plan.rooms() != null
                        ? plan.rooms()
                        : 1,

                normalizeString(
                        plan.hotelName()
                ),

                normalizeString(
                        plan.bookingId()
                ),

                question
        );
    }

    private String normalizeString(
            String value) {

        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        return trimmed.isEmpty()
                ? null
                : trimmed;
    }

    private String cleanJson(
            String response) {

        if (response == null ||
                response.isBlank()) {

            throw new IllegalArgumentException(
                    "LLM returned empty response"
            );
        }

        return response
                .replace("```json", "")
                .replace("```", "")
                .trim();
    }

    }
