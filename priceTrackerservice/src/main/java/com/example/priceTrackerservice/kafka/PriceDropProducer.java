package com.example.priceTrackerservice.kafka;

import org.example.common.dto.PriceDropDetectedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PriceDropProducer {
    private final KafkaTemplate<String, PriceDropDetectedEvent> kafkaTemplate;

    public PriceDropProducer(KafkaTemplate<String, PriceDropDetectedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendPriceDropEvent(PriceDropDetectedEvent event) {
        kafkaTemplate.send("price-drop-detected", event);
    }
}
