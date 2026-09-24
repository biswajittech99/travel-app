package com.example.userservice.service;

import com.example.userservice.repository.UserRepository;
import org.example.common.dto.BookingConfirmedEvent;
import org.example.common.dto.UserNotificationEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class BookingEventConsumer {
    private final KafkaTemplate<String, UserNotificationEvent> kafkaTemplate;
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    @Autowired
    public BookingEventConsumer(KafkaTemplate<String, UserNotificationEvent> kafkaTemplate, UserRepository userRepository,PasswordEncoder encoder) {
        this.kafkaTemplate = kafkaTemplate;
        this.userRepository = userRepository;
        this.encoder= encoder;
    }

    @Value("${app.topics.user-notification}")
    private String userNotificationTopic;

    @KafkaListener(topics = "${app.topics.booking-confirmed}", groupId = "user-service-group")
    public void handleBookingConfirmed(BookingConfirmedEvent event) {
        var user = userRepository.findById(event.userId()).orElse(null);
        if (user == null) return;

        String subject = "Booking Confirmed";
        String body = "Hi " + user.getName() + ",\n\nYour booking " + event.bookingUid() +
                " for flight " + event.flightId() + " is confirmed.\nSeats: " + event.seats();

        UserNotificationEvent notification = new UserNotificationEvent(
                user.getEmail(),
                subject,
                body
        );

        kafkaTemplate.send(userNotificationTopic, user.getId(), notification);
    }
}
