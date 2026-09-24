package com.example.chatbotservice.rag.embedding;

import com.example.chatbotservice.rag.chunk.SpringAiDocumentChunker;
import com.example.chatbotservice.rag.hash.service.DocumentHashService;
import com.example.chatbotservice.rag.ingestion.MarkdownLoader;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmbeddingService {

    private final MarkdownLoader markdownLoader;
    private final SpringAiDocumentChunker chunker;
    private final VectorStore vectorStore;
    private final DocumentHashService hashService;

    public EmbeddingService(
            MarkdownLoader markdownLoader,
            SpringAiDocumentChunker chunker,
            VectorStore vectorStore,
            DocumentHashService hashService) {

        this.markdownLoader = markdownLoader;
        this.chunker = chunker;
        this.vectorStore = vectorStore;
        this.hashService = hashService;
    }

    public void ingestKnowledge() {

        List<Document> documents =
                markdownLoader.loadDocuments();

        int totalChunks = 0;

        for (Document document : documents) {

            if (!hashService.hasChanged(document)) {

                System.out.println(
                        "Skipping : "
                                + document.getMetadata().get("fileName"));

                continue;
            }

            List<Document> chunks =
                    chunker.split(List.of(document));

            if (chunks.isEmpty()) {

                System.out.println(
                        "Skipping empty document : "
                                + document.getMetadata().get("fileName"));

                continue;
            }
            System.out.println("--------------------");
            System.out.println(document.getMetadata().get("fileName"));
            System.out.println("Chunks = " + chunks.size());
            System.out.println("--------------------");

            vectorStore.add(chunks);

            hashService.save(document);

            totalChunks += chunks.size();

            System.out.println(
                    "Indexed : "
                            + document.getMetadata().get("fileName"));
        }

        System.out.println();
        System.out.println("========================");
        System.out.println("Documents : " + documents.size());
        System.out.println("Chunks : " + totalChunks);
        System.out.println("========================");
    }
}
