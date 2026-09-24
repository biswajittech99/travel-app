package com.example.chatbotservice.controller;

import com.example.chatbotservice.controller.dto.ChatRequest;
import com.example.chatbotservice.entity.Conversation;
import com.example.chatbotservice.rag.chat.TravelChatService;
import com.example.chatbotservice.rag.memory.ConversationMemoryService;
import com.example.chatbotservice.rag.memory.ConversationMessage;
import com.example.chatbotservice.repo.ConversationRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final TravelChatService travelChatService;
    private final ConversationRepository conversationRepository;
    private final ConversationMemoryService memoryService;

    public ChatController(
            TravelChatService travelChatService,
            ConversationRepository conversationRepository,
            ConversationMemoryService memoryService) {

        this.travelChatService = travelChatService;
        this.conversationRepository = conversationRepository;
        this.memoryService = memoryService;
    }

    // --------------------------------------------------------
    // Start Conversation
    // --------------------------------------------------------

    @PostMapping("/start")
    public ResponseEntity<Map<String, String>> startConversation(

            @RequestHeader("X-User-Id")
            String userId) {

        Conversation conversation = new Conversation();

        conversation.setUserId(userId);
        conversation.setMode("rag");
        conversation.setMessagesJson("[]");
        conversation.setCreatedAt(LocalDateTime.now());
        conversation.setUpdatedAt(LocalDateTime.now());

        conversationRepository.save(conversation);

        return ResponseEntity.ok(

                Map.of(
                        "conversationId",
                        conversation.getId()
                ));
    }

    // --------------------------------------------------------
    // Chat
    // --------------------------------------------------------

    @PostMapping("/message")
    public ResponseEntity<Map<String, String>> message(

            @RequestHeader("X-User-Id")
            String userId,

            @RequestBody
            ChatRequest request) throws JsonProcessingException {

        String answer =
                travelChatService.chat(
                        userId,
                        request.conversationId(),
                        request.message());

        return ResponseEntity.ok(
                Map.of("reply", answer));
    }

    // --------------------------------------------------------
    // History
    // --------------------------------------------------------

    @GetMapping("/history/{conversationId}")
    public ResponseEntity<List<ConversationMessage>> history(

            @PathVariable
            String conversationId) {

        return ResponseEntity.ok(

                memoryService.load(
                        conversationId));
    }

    // --------------------------------------------------------
    // Clear Conversation
    // --------------------------------------------------------

    @DeleteMapping("/{conversationId}")
    public ResponseEntity<String> clearConversation(

            @PathVariable
            String conversationId) {

        memoryService.delete(conversationId);

        return ResponseEntity.ok(
                "Conversation deleted successfully.");
    }
}
