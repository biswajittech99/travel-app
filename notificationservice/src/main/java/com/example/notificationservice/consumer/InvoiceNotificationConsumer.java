package com.example.notificationservice.consumer;

import org.example.common.dto.InvoiceGeneratedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InvoiceNotificationConsumer {
    private final EmailService emailService;

    public InvoiceNotificationConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    @KafkaListener(
            topics = "invoice-generated",
            groupId = "notification-group"
    )
    public void consume(InvoiceGeneratedEvent event) {

        String subject = "🧾 Your Booking Invoice";

        String body = """
                Dear Customer,

                Your invoice has been generated successfully.

                📄 Invoice ID: %s
                📦 Booking ID: %s

                You can download your invoice here:
                %s

                Regards,
                Travel App Team
                """.formatted(
                event.invoiceId(),
                event.bookingUid(),
                event.pdfPath()
        );

        emailService.sendInvoiceEmail(event
        );
    }
}
