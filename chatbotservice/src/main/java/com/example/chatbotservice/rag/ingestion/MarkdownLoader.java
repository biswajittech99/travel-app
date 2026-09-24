package com.example.chatbotservice.rag.ingestion;


import com.example.chatbotservice.rag.chunk.TravelDocumentChunk;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
public class MarkdownLoader {


    private static final String KNOWLEDGE_PATH =
            "/home/biswajit/Documents/travel-app/aiKnowledge/travel-docs";

    public List<Document> loadDocuments() {

        List<Document> documents = new ArrayList<>();

        try {

            Files.walk(Paths.get(KNOWLEDGE_PATH))
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".md"))
                    .forEach(path -> {

                        try {

                            String content = Files.readString(path);
                            // Ignore empty markdown files
                            if (content.isBlank()) {

                                System.out.println(
                                        "Ignoring empty file : "
                                                + path.getFileName());

                                return;
                            }


                            Document document = new Document(content);

                            document.getMetadata().put(
                                    "fileName",
                                    path.getFileName().toString());

                            document.getMetadata().put(
                                    "source",
                                    path.toAbsolutePath().toString());

                            documents.add(document);

                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                    });

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return documents;
    }
}
