package com.example.chatbotservice.rag.classifier;

import org.springframework.stereotype.Component;

@Component
public class IntentClassifier {

    public boolean needsTool(String question) {

        String q = question.toLowerCase();

        return q.contains("booking")
                || q.contains("book")
                || q.contains("cancel")
                || q.contains("flight")
                || q.contains("hotel")
                || q.contains("my profile")
                || q.contains("my bookings")
                || q.contains("status");

    }

}
