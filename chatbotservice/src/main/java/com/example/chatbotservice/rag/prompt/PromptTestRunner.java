package com.example.chatbotservice.rag.prompt;

import com.example.chatbotservice.rag.retrieval.TravelRetrieverService;
import org.springframework.ai.document.Document;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

//@Component
//public class PromptTestRunner implements CommandLineRunner {

//    private final TravelRetrieverService retriever;
//    private final PromptBuilder promptBuilder;
//
//    public PromptTestRunner(
//            TravelRetrieverService retriever,
//            PromptBuilder promptBuilder) {
//
//        this.retriever = retriever;
//        this.promptBuilder = promptBuilder;
//    }
//
//
//
//
//    @Override
//    public void run(String... args) throws Exception {
//
//        List<Document> docs =
//                retriever.retrieve("Best beaches in Goa");
//
//        String prompt =
//                promptBuilder.buildPrompt(
//                        "Best beaches in Goa",
//                        docs);
//
//        System.out.println();
//        System.out.println("=========== FINAL PROMPT ===========");
//        System.out.println(prompt);
//        System.out.println("====================================");
//    }
//

//}
