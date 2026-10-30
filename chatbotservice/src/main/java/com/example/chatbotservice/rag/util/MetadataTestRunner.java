package com.example.chatbotservice.rag.util;

import com.example.chatbotservice.rag.chunk.SpringAiDocumentChunker;
import com.example.chatbotservice.rag.ingestion.MarkdownLoader;
import org.springframework.ai.document.Document;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

public class MetadataTestRunner implements CommandLineRunner {

    private final MarkdownLoader loader;
    private final SpringAiDocumentChunker chunker;
    private final MetadataExtractor extractor;

    public MetadataTestRunner(
            MarkdownLoader loader,
            SpringAiDocumentChunker chunker,
            MetadataExtractor extractor) {

        this.loader = loader;
        this.chunker = chunker;
        this.extractor = extractor;
    }
    @Override
    public void run(String... args) throws Exception {

        List<Document> docs = loader.loadDocuments();

        List<Document> chunks = chunker.split(docs);

        System.out.println();
        System.out.println("========== METADATA ==========");

        for (Document chunk : chunks) {

            Document enriched = extractor.enrich(chunk);

            System.out.println(enriched.getMetadata());

        }

        System.out.println("==============================");
    }


}
