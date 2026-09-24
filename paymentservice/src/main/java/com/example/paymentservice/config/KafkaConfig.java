package com.example.paymentservice.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.example.common.dto.PaymentRequest;
import org.example.common.dto.PaymentResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;  // ✅ not hardcoded

    // ─── ObjectMapper ───────────────────────────────
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return om;
    }

    // ─── PRODUCER: PaymentResult ─────────────────────
    @Bean
    public ProducerFactory<String, PaymentResult> producerFactory(
            ObjectMapper om) {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.ACKS_CONFIG, "all");        // ✅ durability
        props.put(ProducerConfig.RETRIES_CONFIG, 3);         // ✅ retry on fail
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true); // ✅ no duplicates
        return new DefaultKafkaProducerFactory<>(
                props,
                new StringSerializer(),
                new JsonSerializer<>(om)
        );
    }

    @Bean
    public KafkaTemplate<String, PaymentResult> paymentKafkaTemplate(
            ProducerFactory<String, PaymentResult> pf) {
        return new KafkaTemplate<>(pf);
    }

    // ─── CONSUMER: PaymentRequest ─────────────────────
    @Bean
    public ConsumerFactory<String, PaymentRequest> paymentRequestConsumerFactory(
            ObjectMapper om) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "payment-service-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // ✅ manual ack

        JsonDeserializer<PaymentRequest> deserializer =
                new JsonDeserializer<>(PaymentRequest.class, om);
        deserializer.addTrustedPackages("org.example.common.dto");

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                deserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentRequest>
    paymentRequestListenerFactory(
            ConsumerFactory<String, PaymentRequest> cf) {

        ConcurrentKafkaListenerContainerFactory<String, PaymentRequest> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(cf);
        factory.getContainerProperties()
                .setAckMode(ContainerProperties.AckMode.MANUAL); // ✅ manual ack
        return factory;
    }
}