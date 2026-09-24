//package com.example.chatbotservice.service;
//
//import com.example.chatbotservice.entity.Conversation;
//import com.example.chatbotservice.repo.ConversationRepository;
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Service;
//
//import java.time.Duration;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//@Service
//public class RawApiChatService implements ChatService{
//
//    private final ConversationRepository conversationRepo;
//    private final ClaudeAiService claudeAiService;
//    private final ObjectMapper objectMapper;
//    private final RedisTemplate<String, Object> redisTemplate;
//
//    @Value("${chatbot.cache.conversation-ttl}")
//    private long conversationTtl;
//
//    private static final String CONV_PREFIX = "conv:raw:";
//
//    public RawApiChatService(ConversationRepository conversationRepo,
//                             ClaudeAiService claudeAiService,
//                             ObjectMapper objectMapper,
//                             RedisTemplate<String, Object> redisTemplate) {
//        this.conversationRepo = conversationRepo;
//        this.claudeAiService = claudeAiService;
//        this.objectMapper = objectMapper;
//        this.redisTemplate = redisTemplate;
//    }
//
//
//    @Override
//    public String startConversation(String userId) {
//        Conversation conv = new Conversation();
//        conv.setUserId(userId);
//        conv.setMode("raw-api");
//        conv.setMessagesJson("[]");
//        conv.setCreatedAt(LocalDateTime.now());
//        conv.setUpdatedAt(LocalDateTime.now());
//        conversationRepo.save(conv);
//        return conv.getId();
//    }
//
//    @Override
//    public String processMessage(String userId, String conversationId, String userMessage) {
//        List<Map<String, Object>> messages =
//                loadFromRedis(conversationId);
//
//        if (messages == null) {
//            Conversation conv = conversationRepo
//                    .findById(conversationId)
//                    .orElseThrow(() ->
//                            new RuntimeException("Conversation not found"));
//            messages = parseMessages(conv.getMessagesJson());
//        }
//
//        // Add user message
//        messages.add(Map.of("role", "user", "content", userMessage));
//
//        // Call Raw Claude API
//        String reply = claudeAiService.chat(userId, messages);
//
//        // Add reply to history
//        messages.add(Map.of("role", "assistant", "content", reply));
//
//        // Save to Redis + MySQL
//        saveToRedis(conversationId, messages);
//        saveToMySQL(conversationId, messages);
//
//        return reply;
//    }
//
//    @Override
//    public List<Map<String, String>> getHistory(String conversationId) {
//
//        List<Map<String, Object>> messages =
//                loadFromRedis(conversationId);
//
//        if (messages == null) {
//            Conversation conv = conversationRepo
//                    .findById(conversationId)
//                    .orElseThrow(() ->
//                            new RuntimeException("Conversation not found"));
//            messages = parseMessages(conv.getMessagesJson());
//        }
//
//        return messages.stream()
//                .filter(m -> m.get("content") instanceof String)
//                .map(m -> Map.of(
//                        "role", (String) m.get("role"),
//                        "content", (String) m.get("content")
//                ))
//                .collect(Collectors.toList());
//    }
//
//    private List<Map<String, Object>> loadFromRedis(String convId) {
//        try {
//            Object cached = redisTemplate.opsForValue()
//                    .get(CONV_PREFIX + convId);
//            if (cached != null) {
//                return objectMapper.convertValue(cached,
//                        new TypeReference<>() {});
//            }
//        } catch (Exception e) {
//            System.out.println("Redis read failed: " + e.getMessage());
//        }
//        return null;
//    }
//
//    private void saveToRedis(String convId,
//                             List<Map<String, Object>> messages) {
//        try {
//            List<Map<String, Object>> trimmed = messages.size() > 20
//                    ? messages.subList(messages.size() - 20, messages.size())
//                    : messages;
//            redisTemplate.opsForValue().set(
//                    CONV_PREFIX + convId, trimmed,
//                    Duration.ofSeconds(conversationTtl)
//            );
//        } catch (Exception e) {
//            System.out.println("Redis write failed: " + e.getMessage());
//        }
//    }
//
//    @Async
//    public void saveToMySQL(String convId,
//                            List<Map<String, Object>> messages) {
//        try {
//            Conversation conv = conversationRepo
//                    .findById(convId)
//                    .orElse(new Conversation());
//            conv.setMessagesJson(
//                    objectMapper.writeValueAsString(messages));
//            conv.setUpdatedAt(LocalDateTime.now());
//            conversationRepo.save(conv);
//        } catch (Exception e) {
//            System.out.println("MySQL save failed: " + e.getMessage());
//        }
//    }
//
//    private List<Map<String, Object>> parseMessages(String json) {
//        try {
//            return objectMapper.readValue(json,
//                    new TypeReference<>() {});
//        } catch (Exception e) {
//            return new ArrayList<>();
//        }
//    }
//}
