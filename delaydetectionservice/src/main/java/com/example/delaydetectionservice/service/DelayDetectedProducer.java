package com.example.delaydetectionservice.service;

import lombok.RequiredArgsConstructor;
import org.example.common.dto.DelayDetectedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DelayDetectedProducer {
    private final KafkaTemplate<String, DelayDetectedEvent> kafkaTemplate;

    public void publish(DelayDetectedEvent event) {
        kafkaTemplate.send(
                "flight.delay.detected",
                event.flightId(),
                event
        );
    }
}
