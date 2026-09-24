package com.example.chatbotservice.rag.chunk;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SpringAiDocumentChunker {


    private final TokenTextSplitter splitter = new TokenTextSplitter();



    public List<Document> split(List<Document> documents) {

        return splitter.apply(documents);
    }
}


