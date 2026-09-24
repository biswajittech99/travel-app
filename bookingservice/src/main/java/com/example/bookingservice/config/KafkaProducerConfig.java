package com.example.bookingservice.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.example.common.dto.BookingConfirmedEvent;
import org.example.common.dto.BookingFailedEvent;
import org.example.common.dto.FlightInventoryDTO;
import org.example.common.dto.PaymentRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;


import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaProducerConfig {
    // ✅ Common Consumer configuration
    // ✅ Common Consumer configuration
    @Bean
    public ConsumerFactory<String, FlightInventoryDTO> flightInventoryConsumerFactory() {
        JsonDeserializer<FlightInventoryDTO> deserializer =
                new JsonDeserializer<>(FlightInventoryDTO.class);
        deserializer.addTrustedPackages("*");
        deserializer.setRemoveTypeHeaders(false);  // ✅ must be false when using headers
        deserializer.setUseTypeMapperForKey(true);

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "booking-service-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, FlightInventoryDTO> flightInventoryKafkaListenerFactory()
    { ConcurrentKafkaListenerContainerFactory<String, FlightInventoryDTO> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(flightInventoryConsumerFactory()); return factory;
    }

    @Bean
    public ProducerFactory<String, BookingConfirmedEvent> bookingProducerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, BookingConfirmedEvent> bookingKafkaTemplate() {
        return new KafkaTemplate<>(bookingProducerFactory());
    }

    @Bean
    public ConsumerFactory<String, BookingConfirmedEvent> bookingConfirmedConsumerFactory()
    { Map<String, Object> props = new HashMap<>(); props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "booking-service-group"); props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>( props, new StringDeserializer(), new JsonDeserializer<>(BookingConfirmedEvent.class)
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, BookingConfirmedEvent> bookingConfirmedKafkaListenerFactory()
    {
        ConcurrentKafkaListenerContainerFactory<String, BookingConfirmedEvent> factory = new ConcurrentKafkaListenerContainerFactory<>(); factory.setConsumerFactory(bookingConfirmedConsumerFactory());
        return factory;
    }
    @Bean
    public ProducerFactory<String, PaymentRequest> paymentProducerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, PaymentRequest> paymentKafkaTemplate() {
        return new KafkaTemplate<>(paymentProducerFactory());
    }

    @Bean
    public ProducerFactory<String, BookingFailedEvent> bookingFailedProducerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, BookingFailedEvent> bookingFailedKafkaTemplate() {
        return new KafkaTemplate<>(bookingFailedProducerFactory());
    }


}




