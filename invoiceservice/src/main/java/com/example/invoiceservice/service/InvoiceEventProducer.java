package com.example.invoiceservice.service;

import org.example.common.dto.InvoiceGeneratedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class InvoiceEventProducer {
    private static final String TOPIC = "invoice-generated";

    private final KafkaTemplate<String, InvoiceGeneratedEvent> kafkaTemplate;

    public InvoiceEventProducer(KafkaTemplate<String, InvoiceGeneratedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(InvoiceGeneratedEvent event) {
        kafkaTemplate.send(
                TOPIC,
                event.invoiceId(),
                event
        );
    }
}
