package com.example.chatbotservice.rag.embedding;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class EmbeddingRunner  implements CommandLineRunner {

    private final EmbeddingService embeddingService;

    @Value("${chatbot.knowledge.ingestion.enabled:false}")
    private boolean ingestionEnabled;

    public EmbeddingRunner(
            EmbeddingService embeddingService) {

        this.embeddingService = embeddingService;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!ingestionEnabled) {
            return;
        }

        embeddingService.ingestKnowledge();
    }
}
