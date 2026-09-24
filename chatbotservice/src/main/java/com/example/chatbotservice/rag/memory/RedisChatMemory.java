package com.example.chatbotservice.rag.memory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;




@Component
public class RedisChatMemory implements  ConversationMemoryService {

    private static final String PREFIX = "chat:memory:";
    private static final int MAX_HISTORY = 20;

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${chatbot.cache.conversation-ttl}")
    private long ttlSeconds;

    public RedisChatMemory(
            RedisTemplate<String, Object> redisTemplate,
            ObjectMapper objectMapper) {

        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void addUserMessage(String conversationId, String message) {


        addMessage(
                conversationId,
                "user",
                message);
    }

    @Override
    public void addAssistantMessage(String conversationId, String message) {

        addMessage(
                conversationId,
                "assistant",
                message);
    }

    private void addMessage(
            String conversationId,
            String role,
            String content) {

        List<ConversationMessage> history =
                load(conversationId);

        history.add(
                new ConversationMessage(
                        role,
                        content,
                        LocalDateTime.now()));

        trim(history);

        save(conversationId, history);
    }

    @Override
    public List<ConversationMessage> load(String conversationId) {
        try {

            Object cached =
                    redisTemplate.opsForValue()
                            .get(PREFIX + conversationId);

            if (cached == null) {
                return new ArrayList<>();
            }

            return objectMapper.convertValue(
                    cached,
                    new TypeReference<List<ConversationMessage>>() {});

        } catch (Exception ex) {

            ex.printStackTrace();

            return new ArrayList<>();
        }
    }

    @Override
    public void clear(String conversationId) {

        save(
                conversationId,
                new ArrayList<>());

    }

    @Override
    public void delete(String conversationId) {
        redisTemplate.delete(
                PREFIX + conversationId);
    }

    private void save(
            String conversationId,
            List<ConversationMessage> history) {

        redisTemplate.opsForValue().set(
                PREFIX + conversationId,
                history,
                Duration.ofSeconds(ttlSeconds));
    }

    private void trim(
            List<ConversationMessage> history) {

        if (history.size() <= MAX_HISTORY) {
            return;
        }

        int from =
                history.size() - MAX_HISTORY;

        List<ConversationMessage> latest =
                new ArrayList<>(
                        history.subList(
                                from,
                                history.size()));

        history.clear();

        history.addAll(latest);
    }



}
