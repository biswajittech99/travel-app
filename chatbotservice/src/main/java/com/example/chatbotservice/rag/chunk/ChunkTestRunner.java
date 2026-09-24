//package com.example.chatbotservice.rag.chunk;
//
//
//
//import com.example.chatbotservice.rag.ingestion.MarkdownLoader;
//import org.springframework.ai.document.Document;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
//@Component
//public class ChunkTestRunner implements CommandLineRunner {
//
//    private final MarkdownLoader loader;
//    private final SpringAiDocumentChunker chunker;
//
//    public ChunkTestRunner(
//            MarkdownLoader loader,
//            SpringAiDocumentChunker chunker) {
//
//        this.loader = loader;
//        this.chunker = chunker;
//
//
//    }
//
//    @Override
//    public void run(String... args) throws Exception {
//
//        List<Document> docs = loader.loadDocuments();
//
//        System.out.println("Docs = " + docs.size());
//
//        for (Document doc : docs) {
//            System.out.println("--------------------------------");
//            System.out.println(doc.getMetadata().get("fileName"));
//            System.out.println("Length = " + doc.getText().length());
//        }
//
//        List<Document> chunks = chunker.split(docs);
//
//        System.out.println();
//        System.out.println("=========== CHUNKS ===========");
//
//        int i = 1;
//
//        for (Document chunk : chunks) {
//
//            System.out.println();
//
//            System.out.println("Chunk : " + i++);
//
//            System.out.println("Length : "
//                    + chunk.getText().length());
//
//            System.out.println("Metadata : "
//                    + chunk.getMetadata());
//
//            String preview = chunk.getText();
//
//            if (preview.length() > 120) {
//                preview = preview.substring(0,120);
//            }
//
//            System.out.println(preview);
//
//            System.out.println("----------------------------");
//        }
//
//        System.out.println("Total Chunks : "
//                + chunks.size());
//
//        System.out.println("==============================");
//    }
//}
