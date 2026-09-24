package com.example.chatbotservice.rag.advisor;

import com.example.chatbotservice.rag.retrieval.TravelRetrieverService;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RagAdvisor {

    private final TravelRetrieverService retriever;

    public RagAdvisor(TravelRetrieverService retriever) {
        this.retriever = retriever;
    }

    public List<Document> retrieve(String question) {
        return retriever.retrieve(question);
    }
}
