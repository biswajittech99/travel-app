package com.example.bookingservice.service;


import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RedisRateLimiter {
    private final StringRedisTemplate redis;
    private final int limit = 10; // attempts
    private final Duration window = Duration.ofMinutes(1);

    public RedisRateLimiter(StringRedisTemplate redis) {
        this.redis = redis;
    }

    /**
     * Try to acquire one token for given userId.
     * Returns true if allowed, false if rate-limited.
     */
    public boolean tryAcquire(String userId) {
        String key = "rl:booking:" + userId;
        Long count = redis.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redis.expire(key, window);
        }
        return count != null && count <= limit;
    }

    public void reset(String userId) {
        redis.delete("rl:booking:" + userId);
    }
}
