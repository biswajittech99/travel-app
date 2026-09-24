package com.example.chatbotservice.rag.hash.model;

import java.time.LocalDateTime;

public class DocumentHash {

    private String fileName;

    private String hash;

    private LocalDateTime indexedAt;

    public DocumentHash() {
    }

    public DocumentHash(String fileName,
                        String hash,
                        LocalDateTime indexedAt) {
        this.fileName = fileName;
        this.hash = hash;
        this.indexedAt = indexedAt;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public LocalDateTime getIndexedAt() {
        return indexedAt;
    }

    public void setIndexedAt(LocalDateTime indexedAt) {
        this.indexedAt = indexedAt;
    }
}
