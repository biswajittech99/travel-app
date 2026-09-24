package com.example.chatbotservice.rag.memory;

import java.util.List;

public interface ConversationMemoryService {


    void addUserMessage(
            String conversationId,
            String message);

    void addAssistantMessage(
            String conversationId,
            String message);

    List<ConversationMessage> load(
            String conversationId);

    void clear(
            String conversationId);

    void delete(
            String conversationId);

}
