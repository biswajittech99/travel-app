package com.example.chatbotservice.rag.advisor;

import com.example.chatbotservice.rag.memory.ConversationMessage;
import com.example.chatbotservice.rag.memory.RedisChatMemory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ConversationAdvisor {

    private final RedisChatMemory chatMemory;

    public ConversationAdvisor(RedisChatMemory chatMemory) {
        this.chatMemory = chatMemory;
    }

    public List<ConversationMessage> history(String conversationId) {
        return chatMemory.load(conversationId);
    }

    public void saveUser(String conversationId,
                         String message) {

        chatMemory.addUserMessage(
                conversationId,
                message);
    }

    public void saveAssistant(String conversationId,
                              String message) {

        chatMemory.addAssistantMessage(
                conversationId,
                message);
    }

}
