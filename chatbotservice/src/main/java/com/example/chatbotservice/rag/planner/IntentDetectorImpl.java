package com.example.chatbotservice.rag.planner;

import org.springframework.stereotype.Component;

@Component
public class IntentDetectorImpl  implements  IntentDetector{


    @Override
    public IntentType detect(String question) {
        String q = question.toLowerCase();

        if (q.contains("flight")) {
            return IntentType.FLIGHT_SEARCH;
        }

        if (q.contains("hotel")) {
            return IntentType.HOTEL_SEARCH;
        }

        if (q.contains("book hotel")) {
            return IntentType.BOOK_HOTEL;
        }

        if (q.contains("book flight")) {
            return IntentType.BOOK_FLIGHT;
        }

        if (q.contains("cancel")) {
            return IntentType.CANCEL_BOOKING;
        }

        if (q.contains("what is")
                || q.contains("tell me about")
                || q.contains("recommend")) {

            return IntentType.KNOWLEDGE;
        }

        return IntentType.GENERAL_CHAT;
    }

}
