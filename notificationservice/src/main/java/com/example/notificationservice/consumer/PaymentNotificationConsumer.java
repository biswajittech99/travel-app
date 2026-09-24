package com.example.notificationservice.consumer;

import org.example.common.dto.PaymentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class PaymentNotificationConsumer {
    private final EmailService emailService;
    private final Logger log = LoggerFactory.getLogger(getClass());

    public PaymentNotificationConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    @KafkaListener(
            topics = "payment.succeeded",
            groupId = "notification-group",
            containerFactory = "paymentResultKafkaListenerContainerFactory"
    )
    public void handlePaymentSuccess(PaymentResult event) {
        try {
            log.info("💰 Received PAYMENT SUCCESS event for {}", event.bookingUid());

            String subject = "Payment Successful 💳";
            String body = """
                    Dear User,

                    Your payment has been successfully processed.

                    Booking ID: %s
                    Amount: %.2f %s
                    Provider Reference: %s

                    Thank you for choosing our service!
                    """.formatted(
                    event.bookingUid(),
                    event.amount(),
                    event.currency(),
                    event.providerPaymentId()
            );

            emailService.sendEmail(event.userEmail(), subject, body);

            log.info("📤 Payment success email sent to {}", event.userEmail());
        } catch (Exception ex) {
            log.error("❌ Failed to send payment success email", ex);
        }
    }

    @KafkaListener(
            topics = "payment.failed",
            groupId = "notification-group",
            containerFactory = "paymentResultKafkaListenerContainerFactory"
    )
    public void handlePaymentFailure(PaymentResult event) {
        try {
            log.info("⚠️ Received PAYMENT FAILURE event for {}", event.bookingUid());

            String subject = "Payment Failed ⚠️";
            String body = """
                    Dear User,

                    Unfortunately, your payment could not be processed.

                    Booking ID: %s
                    Amount: %.2f %s
                    Reason: %s

                    Please try again or contact support.
                    """.formatted(
                    event.bookingUid(),
                    event.amount(),
                    event.currency(),
                    event.errorMessage()
            );

            emailService.sendEmail(event.userEmail(), subject, body);

            log.info("📤 Payment failure email sent to {}", event.userEmail());
        } catch (Exception ex) {
            log.error("❌ Failed to send payment failure email", ex);
        }
    }
}
