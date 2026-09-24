package com.example.chatbotservice.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class LiveSearchContextRedisService {

    private static final String KEY_PREFIX =
            "chat:live-search:";

    private static final Duration SEARCH_TTL =
            Duration.ofMinutes(15);

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public LiveSearchContextRedisService(
            RedisTemplate<String, String> redisTemplate,
            ObjectMapper objectMapper
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void save(
            String conversationId,
            PendingBookingType type,
            Object searchData
    ) {

        try {

            LiveSearchContext context =
                    new LiveSearchContext(
                            conversationId,
                            type,
                            objectMapper.writeValueAsString(
                                    searchData
                            ),
                            LocalDateTime.now()
                    );

            redisTemplate.opsForValue().set(
                    buildKey(conversationId),
                    objectMapper.writeValueAsString(context),
                    SEARCH_TTL
            );

        } catch (Exception ex) {

            throw new IllegalStateException(
                    "Failed to save live search context",
                    ex
            );
        }
    }

    public Optional<LiveSearchContext> find(
            String conversationId
    ) {

        try {

            String value =
                    redisTemplate.opsForValue()
                            .get(buildKey(conversationId));

            if (value == null || value.isBlank()) {
                return Optional.empty();
            }

            return Optional.of(
                    objectMapper.readValue(
                            value,
                            LiveSearchContext.class
                    )
            );

        } catch (Exception ex) {

            throw new IllegalStateException(
                    "Failed to load live search context",
                    ex
            );
        }
    }

    public void delete(String conversationId) {

        redisTemplate.delete(
                buildKey(conversationId)
        );
    }

    private String buildKey(
            String conversationId
    ) {

        return KEY_PREFIX + conversationId;
    }
}
