package com.example.bookingservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Booking Service is the PRODUCER for these topics, so it owns their
 * creation. Declaring them here means they exist the moment this
 * service starts, regardless of the broker's auto.create.topics.enable
 * setting — instead of only coming into existence the first time a
 * real booking resolves. Consumers (chatbot-service) starting before
 * that first event would otherwise poll UNKNOWN_TOPIC_OR_PARTITION
 * until it eventually gets created on the fly.
 */


@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic paymentRequestTopic() {
        return TopicBuilder.name("payment.request")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic bookingConfirmedTopic() {
        return TopicBuilder.name("booking-confirmed-topic")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic bookingFailedTopic() {
        return TopicBuilder.name("booking-failed-topic")
                .partitions(3)
                .replicas(1)
                .build();
    }
}






