package com.example.chatbotservice.rag.util;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MetadataExtractor {

    public Document enrich(Document chunk) {

        // metadata enrichment will go here
        Map<String, Object> metadata = chunk.getMetadata();

        String fileName =
                (String) metadata.getOrDefault("fileName", "");

        metadata.put("source", fileName);
        metadata.put(
                "docType",
                extractDocumentType(fileName)
        );


        return chunk;
    }

    private String extractDocumentType(String fileName) {

        fileName = fileName.toLowerCase();

        if (fileName.contains("hotel")) {
            return "hotel-guide";
        }

        if (fileName.contains("visa")) {
            return "visa-guide";
        }

        if (fileName.contains("budget")) {
            return "budget-guide";
        }

        if (fileName.contains("packing")) {
            return "packing-guide";
        }

        if (fileName.contains("flight")) {
            return "flight-guide";
        }

        return "travel-guide";
    }
}
