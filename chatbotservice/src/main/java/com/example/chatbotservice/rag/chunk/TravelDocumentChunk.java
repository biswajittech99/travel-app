package com.example.chatbotservice.rag.chunk;

import java.util.HashMap;
import java.util.Map;

public class TravelDocumentChunk {

    private String id;

    private String documentName;

    private String content;

    private int chunkIndex;

    private Map<String, Object> metadata = new HashMap<>();

    public TravelDocumentChunk() {
    }

    public TravelDocumentChunk(String id,
                               String documentName,
                               String content,
                               int chunkIndex,
                               Map<String, Object> metadata) {

        this.id = id;
        this.documentName = documentName;
        this.content = content;
        this.chunkIndex = chunkIndex;
        this.metadata = metadata;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(int chunkIndex) {
        this.chunkIndex = chunkIndex;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
