package com.example.chatbotservice.rag.llm;

public interface LlmService {

    String chat(String prompt);
    LlmResponse chat(LlmRequest request);
}
