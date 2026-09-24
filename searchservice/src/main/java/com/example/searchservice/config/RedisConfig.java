package com.example.searchservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {


    @Bean
    @Primary
    public ReactiveRedisTemplate<String, String> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory factory) {

        RedisSerializationContext<String, String> context =
                RedisSerializationContext.<String, String>
                                newSerializationContext(new StringRedisSerializer())
                        .value(new StringRedisSerializer())  // ✅ String value
                        .build();

        return new ReactiveRedisTemplate<>(factory, context);
    }

    // ✅ Keep Object template for other uses
    @Bean
    public ReactiveRedisTemplate<String, Object> reactiveObjectRedisTemplate(
            ReactiveRedisConnectionFactory factory) {

        var serializer = new GenericJackson2JsonRedisSerializer();
        var context = RedisSerializationContext
                .<String, Object>newSerializationContext(
                        new StringRedisSerializer())
                .value(serializer)
                .build();

        return new ReactiveRedisTemplate<>(factory, context);
    }
}

