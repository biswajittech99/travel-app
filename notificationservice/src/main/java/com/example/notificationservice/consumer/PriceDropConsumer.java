package com.example.notificationservice.consumer;

import org.example.common.dto.InvoiceGeneratedEvent;
import org.example.common.dto.PriceDropDetectedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PriceDropConsumer {
    private final EmailService emailService;

    private final Logger log = LoggerFactory.getLogger(getClass());

    public PriceDropConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    @KafkaListener(
            topics = "price.drop.detected",
            groupId = "notification-group",
            containerFactory = "priceDropKafkaListenerContainerFactory"
    )
    public void consume(@Payload PriceDropDetectedEvent event) {

        try {
            String subject = "💸 Price Drop Alert!";
            String body = String.format("""
                Good news!

                The price for your watched flight has dropped.

                ✈ Flight: %s
                Old Price: ₹%.2f
                New Price: ₹%.2f

                Book now before it increases again!
                """,
                    event.getFlightId(),                   // ✔ correct field
                    event.getTargetPrice().doubleValue(),  // ✔ BigDecimal → double
                    event.getLivePrice().doubleValue()     // ✔ BigDecimal → double
            );

            emailService.sendPriceDropEmail(
                     event
            );

            log.info("✅ Price drop email sent to {}", event.getUserEmail());

        } catch (Exception e) {
            log.error("❌ Failed to send price drop notification", e);
        }
    }
}
