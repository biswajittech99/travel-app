package com.example.userservice.security;


import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
public class KafkaEgarConfig {

    @Bean
    public ApplicationRunner warmUpKafkaProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        return args -> {
            try {
                // Trigger producer metadata fetch at startup, not on first request
                kafkaTemplate.getProducerFactory().createProducer().partitionsFor("send-otp-email-topic");
                System.out.println("✅ Kafka producer warmed up");
            } catch (Exception e) {
                System.out.println("⚠️ Kafka warmup failed (non-fatal): " + e.getMessage());
            }
        };
    }
}
