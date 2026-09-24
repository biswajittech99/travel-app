package com.example.notificationservice.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.example.common.dto.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {
    @Bean
    public ConsumerFactory<String, UserNotificationEvent> userNotificationConsumerFactory() {
        JsonDeserializer<UserNotificationEvent> deserializer =
                new JsonDeserializer<>(UserNotificationEvent.class);
        deserializer.addTrustedPackages("*");
        deserializer.ignoreTypeHeaders(); // important — no headers used

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "notification-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deserializer.getClass());

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserNotificationEvent>
    userNotificationKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, UserNotificationEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(userNotificationConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, SendOtpEmailEvent> sendOtpEmailConsumerFactory() {

        JsonDeserializer<SendOtpEmailEvent> deserializer =
                new JsonDeserializer<>(SendOtpEmailEvent.class);
        deserializer.addTrustedPackages("*");
        deserializer.ignoreTypeHeaders();

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "notification-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deserializer.getClass());

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SendOtpEmailEvent>
    sendOtpEmailKafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, SendOtpEmailEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(sendOtpEmailConsumerFactory());

        // ✅ Stop infinite retries — try 3 times then give up
        FixedBackOff backOff = new FixedBackOff(2000L, 3L); // 2s interval, 3 attempts
        factory.setCommonErrorHandler(new DefaultErrorHandler(backOff));

        return factory;
    }
    @Bean
    public ConsumerFactory<String, BookingConfirmedEvent> bookingConfirmedConsumerFactory() {
        JsonDeserializer<BookingConfirmedEvent> deserializer =
                new JsonDeserializer<>(BookingConfirmedEvent.class);
        deserializer.addTrustedPackages("*");
        deserializer.ignoreTypeHeaders();

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "notification-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deserializer.getClass());

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, BookingConfirmedEvent>
    bookingConfirmedKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, BookingConfirmedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(bookingConfirmedConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, PaymentResult> paymentResultConsumerFactory() {
        JsonDeserializer<PaymentResult> deserializer =
                new JsonDeserializer<>(PaymentResult.class);
        deserializer.addTrustedPackages("*");
        deserializer.ignoreTypeHeaders();

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "notification-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deserializer.getClass());

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentResult>
    paymentResultKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, PaymentResult> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(paymentResultConsumerFactory());
        return factory;
    }
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PriceDropDetectedEvent>
    priceDropKafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, PriceDropDetectedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        JsonDeserializer<PriceDropDetectedEvent> deserializer =
                new JsonDeserializer<>(PriceDropDetectedEvent.class);
        deserializer.addTrustedPackages("*");
        deserializer.ignoreTypeHeaders();

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "notification-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deserializer.getClass());

        ConsumerFactory<String, PriceDropDetectedEvent> factoryConsumer =
                new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);

        factory.setConsumerFactory(factoryConsumer);
        return factory;
    }
}
