package com.example.chatbotservice.rag.retrieval;

import org.springframework.ai.document.Document;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RetrieverTestRunner implements CommandLineRunner {

    private final TravelRetrieverService retriever;

    public RetrieverTestRunner(
            TravelRetrieverService  retriever) {
        this.retriever = retriever;
    }

    @Override
    public void run(String... args) throws Exception {

        List<Document> docs =
                retriever.retrieve("Best beaches in Goa");

        System.out.println();
        System.out.println("========= RETRIEVAL =========");

        for (Document doc : docs) {

            System.out.println(doc.getMetadata());

            System.out.println("----------------");

            String preview = doc.getText();

            if (preview.length() > 200)
                preview = preview.substring(0, 200);

            System.out.println(preview);

            System.out.println();
        }

        System.out.println("=============================");
    }


}
