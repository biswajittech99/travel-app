package com.example.paymentservice.service;

import org.example.common.dto.PaymentRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
public class PaymentRequestConsumer {
    private final PaymentProcessor processor;
    private final Logger log = LoggerFactory.getLogger(getClass());

    public PaymentRequestConsumer(PaymentProcessor processor) {
        this.processor = processor;
    }

    @KafkaListener(
            topics = "${payment.topics.request}",   // payment.request
            groupId = "payment-service-group",
            containerFactory = "paymentRequestListenerFactory"
    )
    public void handlePaymentRequest(
            PaymentRequest req,
            Acknowledgment ack) {           // ✅ manual ack

        log.info("📩 Received payment request for booking: {}",
                req.bookingUid());
        try {
            processor.processAsync(req);
            ack.acknowledge();              // ✅ only ack after processing
            log.info("✅ Payment processed and acked for: {}",
                    req.bookingUid());
        } catch (Exception e) {
            log.error("❌ Payment processing failed for: {}",
                    req.bookingUid(), e);
            // ❌ Do NOT ack → Kafka will redeliver
            // In production: send to Dead Letter Topic after N retries
        }
    }
}
