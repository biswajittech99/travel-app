package com.example.chatbotservice.rag.advisor;

import com.example.chatbotservice.rag.memory.ConversationMessage;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TravelAdvisorChain {
    private final RagAdvisor ragAdvisor;

    private final ConversationAdvisor conversationAdvisor;

    public TravelAdvisorChain(
            RagAdvisor ragAdvisor,
            ConversationAdvisor conversationAdvisor) {

        this.ragAdvisor = ragAdvisor;
        this.conversationAdvisor = conversationAdvisor;
    }

    public List<Document> retrieveKnowledge(
            String question) {

        return ragAdvisor.retrieve(question);
    }

    public List<ConversationMessage> conversation(
            String conversationId) {

        return conversationAdvisor.history(conversationId);
    }

    public void saveUser(
            String conversationId,
            String message) {

        conversationAdvisor.saveUser(
                conversationId,
                message);
    }

    public void saveAssistant(
            String conversationId,
            String message) {

        conversationAdvisor.saveAssistant(
                conversationId,
                message);
    }
}
