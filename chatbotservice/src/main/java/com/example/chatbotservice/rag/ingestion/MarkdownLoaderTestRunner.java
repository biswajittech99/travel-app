//package com.example.chatbotservice.rag.ingestion;
//
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//
//@Component
//public class MarkdownLoaderTestRunner implements CommandLineRunner {
//
//    private final MarkdownLoader markdownLoader;
//
//    public MarkdownLoaderTestRunner(
//            MarkdownLoader markdownLoader) {
//        this.markdownLoader = markdownLoader;
//    }
//
//    @Override
//    public void run(String... args) throws Exception {
//
//        var docs = markdownLoader.loadDocuments();
//
//        System.out.println("--------------------------------");
//
//        System.out.println("Loaded Docs : " + docs.size());
//
//        docs.forEach(doc -> {
//
//            System.out.println(
//                    doc.getMetadata().get("fileName"));
//
//        });
//
//        System.out.println("--------------------------------");
//
//    }
//}
