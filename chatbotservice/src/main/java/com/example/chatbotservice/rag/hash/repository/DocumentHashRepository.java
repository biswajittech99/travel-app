package com.example.chatbotservice.rag.hash.repository;

import com.example.chatbotservice.rag.hash.model.DocumentHash;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class DocumentHashRepository {

    private final Map<String, DocumentHash> store =
            new ConcurrentHashMap<>();

    public Optional<DocumentHash> findByFileName(String fileName) {

        return Optional.ofNullable(store.get(fileName));
    }

    public void save(DocumentHash hash) {

        store.put(hash.getFileName(), hash);
    }

    public boolean exists(String fileName) {

        return store.containsKey(fileName);
    }

}
