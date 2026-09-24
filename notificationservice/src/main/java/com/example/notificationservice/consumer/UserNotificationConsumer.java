package com.example.notificationservice.consumer;

import org.example.common.dto.BookingConfirmedEvent;
import org.example.common.dto.RegistrationSuccessEvent;
import org.example.common.dto.SendOtpEmailEvent;
import org.example.common.dto.UserNotificationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class UserNotificationConsumer {
    private final EmailService emailService;
    private final Logger log = LoggerFactory.getLogger(getClass());

    public UserNotificationConsumer(EmailService emailService) {
        this.emailService = emailService;
    }



    @KafkaListener(
            topics = "send-otp-email-topic",
            groupId = "notification-group",
            containerFactory = "sendOtpEmailKafkaListenerContainerFactory"
    )
    public void receiveOtpEmail(@Payload SendOtpEmailEvent event) {
        log.info("🔐 Received OTP email event for {}", event.email());
        try {
            emailService.sendEmail(
                    event.email(),
                    "Your OTP Code",
                    "Your OTP is: " + event.otp()
            );
            log.info("✅ OTP email sent to {}", event.email());
        } catch (Exception e) {
            log.error("❌ Failed to send OTP email to {}: {}", event.email(), e.getMessage());
            // Don't re-throw — Kafka won't retry infinitely and won't block other messages
        }
    }

    @KafkaListener(topics = "${app.topics.user-notification}", groupId = "notification-group",
            containerFactory = "userNotificationKafkaListenerContainerFactory")
    public void receive(@Payload UserNotificationEvent event) {
        log.info("📩 Received notification for {}", event.email());
        try {
            emailService.sendEmail(event.email(), event.subject(), event.body());
            log.info("✅ Email sent to {}", event.email());
        } catch (Exception e) {
            log.error("❌ Failed to send email to {}: {}", event.email(), e.getMessage());
        }
    }

    @KafkaListener(topics = "user-registration-success-topic", groupId = "notification-group")
    public void handleRegistrationSuccess(RegistrationSuccessEvent event) {
        log.info("🎉 Processing registration for {}", event.email());
        try {
            String subject = "Welcome to Travel App 🎉";
            String message = "Hi " + event.name() + ",\n\n"
                    + "Your account has been created successfully.\n\n"
                    + "Login ID: " + event.email() + "\n\n"
                    + "Thank you for joining us!";
            emailService.sendEmail(event.email(), subject, message);
            log.info("✅ Welcome email sent to {}", event.email());
        } catch (Exception e) {
            log.error("❌ Failed to send welcome email to {}: {}", event.email(), e.getMessage());
        }
    }
    @KafkaListener(
            topics = "booking-confirmed-topic",
            groupId = "notification-group",
            containerFactory = "bookingConfirmedKafkaListenerContainerFactory"
    )
    public void receiveBookingConfirmation(@Payload BookingConfirmedEvent event) {
        try {
            log.info("🛫 Received booking confirmation for user: {}", event.userId());

            String subject = "Your Flight Booking is Confirmed ✈️";
            String body = String.format("""
                Dear User,
                
                Your booking (%s) for flight %s has been successfully confirmed.
                Seats booked: %d
                Price: %.2f
                
                Thank you for choosing our service!
                """,
                    event.bookingUid(),
                    event.flightId(),
                    event.seats(),
                    event.totalAmount()
            );

            // For testing
            String testEmail = "adhikarybiswajit.techno@gmail.com";
            emailService.sendEmail(testEmail, subject, body);

            log.info("✅ Sent booking confirmation email for booking {}", event.bookingUid());
        } catch (Exception e) {
            log.error("❌ Failed to send booking confirmation for {}", event.bookingUid(), e);
        }
    }
}
