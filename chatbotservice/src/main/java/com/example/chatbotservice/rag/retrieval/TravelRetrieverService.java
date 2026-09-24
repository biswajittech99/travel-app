package com.example.chatbotservice.rag.retrieval;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TravelRetrieverService {

    private final VectorStore vectorStore;

    public TravelRetrieverService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public List<Document> retrieve(String question) {

        SearchRequest request = SearchRequest.builder()
                .query(question)
                .topK(5)
                .build();

        return vectorStore.similaritySearch(request);
    }
}
