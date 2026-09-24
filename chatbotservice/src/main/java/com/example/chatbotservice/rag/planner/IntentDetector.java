package com.example.chatbotservice.rag.planner;

public interface IntentDetector {

    IntentType detect(String question);
}
