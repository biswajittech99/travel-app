//package com.example.chatbotservice.service;

import com.example.chatbotservice.entity.Conversation;
import com.example.chatbotservice.rag.prompt.PromptBuilder;
import com.example.chatbotservice.rag.retrieval.TravelRetrieverService;
import com.example.chatbotservice.repo.ConversationRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;


import org.springframework.beans.factory.annotation.Value;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//@Service
//public class SpringAiChatService implements  ChatService{
//
//    private static final Logger log =
//            LoggerFactory.getLogger(SpringAiChatService.class);
//
//    private static final String CONV_PREFIX = "conv:springai:";
//
//    private final ChatClient chatClient;
//    private final ConversationRepository conversationRepository;
//    private final RedisTemplate<String, Object> redisTemplate;
//    private final ObjectMapper objectMapper;
//    private final TravelTools travelTools;
//    private final PromptBuilder promptBuilder;
//
//    @Value("${chatbot.cache.conversation-ttl}")
//    private long conversationTtl;
//
//    public SpringAiChatService(
//            ChatClient.Builder builder,
//            ConversationRepository conversationRepository,
//            RedisTemplate<String, Object> redisTemplate,
//            ObjectMapper objectMapper,
//            TravelTools travelTools,
//            PromptBuilder promptBuilder) {
//
//        this.conversationRepository = conversationRepository;
//        this.redisTemplate = redisTemplate;
//        this.objectMapper = objectMapper;
//        this.travelTools = travelTools;
//        this.promptBuilder = promptBuilder;
//
//        this.chatClient = builder
//                .defaultTools(travelTools)
//                .build();
//    }
//
//
//
//    @Override
//    public String startConversation(String userId) {
//
//        Conversation conversation = new Conversation();
//
//        conversation.setUserId(userId);
//        conversation.setMode("spring-ai");
//        conversation.setMessagesJson("[]");
//        conversation.setCreatedAt(LocalDateTime.now());
//        conversation.setUpdatedAt(LocalDateTime.now());
//
//        conversationRepository.save(conversation);
//
//        return conversation.getId();
//    }
//
//    @Override
//    public String processMessage(String userId, String conversationId, String message) {
//        travelTools.setCurrentUserId(userId);
//
//        List<Map<String, String>> history =
//                loadHistory(conversationId);
//
//        try {
//
//            String systemPrompt =
//                    promptBuilder.buildPrompt(message);
//
//            systemPrompt += """
//
//                    Current User Id:
//                    %s
//
//                    When the user asks about:
//
//                    • My bookings
//                    • My profile
//                    • Flight status
//                    • Booking details
//
//                    Use the available Travel Tools whenever required.
//                    """.formatted(userId);
//
//
//            String reply =
//                    chatClient.prompt()
//                            .system(systemPrompt)
//                            .messages(aiMessages)
//                            .user(message)
//                            .call()
//                            .content();
//
//            history.add(Map.of(
//                    "role",
//                    "user",
//                    "content",
//                    message));
//
//            history.add(Map.of(
//                    "role",
//                    "assistant",
//                    "content",
//                    reply));
//
//            saveHistory(conversationId, history);
//
//            saveToMySQL(
//                    conversationId,
//                    userId,
//                    history);
//
//            return reply;
//
//        } catch (Exception ex) {
//
//            log.error("Chat processing failed", ex);
//
//            return """
//                    Sorry, I couldn't process your request.
//                    Please try again after a few moments.
//                    """;
//        }
//    }
//
//    @Override
//    public List<Map<String, String>> getHistory(String conversationId) {
//
//        return loadHistory(conversationId);
//    }
//
//    private List<Map<String, String>> loadHistory(String conversationId) {
//
//        try {
//
//            Object cache =
//                    redisTemplate.opsForValue()
//                            .get(CONV_PREFIX + conversationId);
//
//            if (cache != null) {
//
//                return objectMapper.convertValue(
//                        cache,
//                        new TypeReference<List<Map<String, String>>>() {
//                        });
//            }
//
//        } catch (Exception ex) {
//
//            log.error("Redis load failed", ex);
//        }
//
//        try {
//
//            Conversation conversation =
//                    conversationRepository.findById(conversationId)
//                            .orElse(null);
//
//            if (conversation != null &&
//                    conversation.getMessagesJson() != null) {
//
//                return objectMapper.readValue(
//                        conversation.getMessagesJson(),
//                        new TypeReference<List<Map<String, String>>>() {
//                        });
//            }
//
//        } catch (Exception ex) {
//
//            log.error("MySQL load failed", ex);
//        }
//
//        return new ArrayList<>();
//    }
//
//    private void saveHistory(String conversationId,
//                             List<Map<String, String>> history) {
//
//        try {
//
//            List<Map<String, String>> trimmed =
//                    history.size() > 20
//                            ? history.subList(
//                            history.size() - 20,
//                            history.size())
//                            : history;
//
//            redisTemplate.opsForValue().set(
//                    CONV_PREFIX + conversationId,
//                    trimmed,
//                    Duration.ofSeconds(conversationTtl));
//
//        } catch (Exception ex) {
//
//            log.error("Redis save failed", ex);
//        }
//    }
//    @Async
//    public void saveToMySQL(String conversationId,
//                            String userId,
//                            List<Map<String, String>> history) {
//
//        try {
//
//            Conversation conversation =
//                    conversationRepository
//                            .findById(conversationId)
//                            .orElse(new Conversation());
//
//            conversation.setUserId(userId);
//
//            conversation.setMessagesJson(
//                    objectMapper.writeValueAsString(history));
//
//            conversation.setUpdatedAt(LocalDateTime.now());
//
//            conversationRepository.save(conversation);
//
//        } catch (Exception ex) {
//
//            log.error("MySQL save failed", ex);
//        }
//    }
//}
