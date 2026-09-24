package com.example.searchservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        // ✅ THIS IS THE KEY FIX
        // false = write as ISO string "2026-06-25T06:30:00Z"
        // true  = write as 1782388800.000 ← your current problem
        mapper.disable(
                SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return mapper;
    }
}
