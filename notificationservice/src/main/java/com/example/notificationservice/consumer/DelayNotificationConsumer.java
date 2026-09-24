package com.example.notificationservice.consumer;


import lombok.RequiredArgsConstructor;
import org.example.common.dto.BookingInfo;
import org.example.common.dto.DelayDetectedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class DelayNotificationConsumer {

    private final EmailService emailService;
    private final Logger log = LoggerFactory.getLogger(getClass());


    public DelayNotificationConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    @KafkaListener(
            topics = "flight.delay.detected",
            groupId = "notification-group"
    )
    public void consume(DelayDetectedEvent event) {
        log.info("📩 Received delay event for booking {}", event.bookingUid());

        String subject = "✈ Flight Delay Alert";

        String body = String.format("""
                Dear Customer,

                Your flight %s has been delayed.

                ⏰ Delay: %d minutes
                📄 Booking ID: %s

                We apologize for the inconvenience.

                Regards,
                Travel App Team
                """,
                event.flightId(),
                event.delayMinutes(),
                event.bookingUid()
        );

        emailService.sendEmail(
                event.userEmail(),
                subject,
                body
        );


        log.info("📧 Delay email sent to {}", event.userEmail());
    }



}
