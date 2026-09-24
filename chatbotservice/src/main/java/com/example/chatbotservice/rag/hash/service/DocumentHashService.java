package com.example.chatbotservice.rag.hash.service;

import com.example.chatbotservice.rag.hash.model.DocumentHash;
import com.example.chatbotservice.rag.hash.repository.DocumentHashRepository;
import com.example.chatbotservice.rag.util.HashUtil;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DocumentHashService {

    private final DocumentHashRepository repository;

    public DocumentHashService(
            DocumentHashRepository repository) {

        this.repository = repository;
    }

    public boolean hasChanged(Document document) {

        String fileName =
                document.getMetadata().get("fileName").toString();

        String currentHash =
                HashUtil.sha256(document.getText());

        return repository
                .findByFileName(fileName)
                .map(existing ->
                        !existing.getHash().equals(currentHash))
                .orElse(true);
    }

    public void save(Document document) {

        String fileName =
                document.getMetadata().get("fileName").toString();

        String hash =
                HashUtil.sha256(document.getText());

        DocumentHash documentHash =
                new DocumentHash(
                        fileName,
                        hash,
                        LocalDateTime.now());

        repository.save(documentHash);
    }

}
