package com.example.userservice.service;

import com.example.userservice.entity.OutboxEvent;
import com.example.userservice.repository.OutboxRepo;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OutboxPublisher {

    private final OutboxRepo repo;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OutboxPublisher(OutboxRepo repo,
                           KafkaTemplate<String, Object> kafkaTemplate) {
        this.repo = repo;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 5000)
    public void publishEvents() {

        List<OutboxEvent> events = repo.findByStatus("PENDING");

        for (OutboxEvent event : events) {
            try {
                kafkaTemplate.send(
                        "user-registration-success-topic",
                        event.getPayload()
                );

                event.setStatus("SENT");
                repo.save(event);

            } catch (Exception ex) {
                // retry later
            }
        }
    }
}
