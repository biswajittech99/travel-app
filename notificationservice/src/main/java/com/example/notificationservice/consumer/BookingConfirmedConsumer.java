//package com.example.notificationservice.consumer;
//
//
//import org.example.common.dto.BookingConfirmedEvent;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Service;
//import org.springframework.web.reactive.function.client.WebClient;
//import reactor.core.publisher.Mono;
//
//@Service
//public class BookingConfirmedConsumer {
//
//    private final WebClient webClient; // to call user-service
//    private final EmailService emailService;
//    private final Logger log = LoggerFactory.getLogger(getClass());
//
//    public BookingConfirmedConsumer(WebClient.Builder builder, EmailService emailService) {
//        this.webClient = builder.baseUrl("http://localhost:8088").build(); // user-service URL
//        this.emailService = emailService;
//    }
//
//    @KafkaListener(topics = "booking-confirmed", groupId = "notification-group")
//    public void receive(BookingConfirmedEvent event) {
//        try {
//            log.info("Received booking-confirmed event: {}", event.bookingUid());
//            // call user-service to get email
//            var userMono = webClient.get()
//                    .uri("/api/users/{id}", event.userId())
//                    .retrieve()
//                    .bodyToMono(UserDto.class)
//                    .onErrorResume(e -> {
//                        log.warn("Failed to fetch user {}, error: {}", event.userId(), e.getMessage());
//                        return Mono.empty();
//                    });
//
//            UserDto user = userMono.block(); // small sync for demo
//            if (user == null) {
//                log.warn("User not found for id: {}", event.userId());
//                // optionally still send to admin email
//                return;
//            }
//
//            String subject = "Booking confirmed: " + event.bookingUid();
//            String body = "Hi " + user.name() + ",\n\n" +
//                    "Your booking " + event.bookingUid() + " for flight " + event.flightId() +
//                    " has been confirmed.\nSeats: " + event.seats() + "\n\nThanks.";
//
//            emailService.sendEmail(user.email(), subject, body);
//
//        } catch (Exception ex) {
//            log.error("Error processing booking-confirmed event", ex);
//            // optionally re-throw to let Kafka retry depending on container config
//        }
//    }
//
//    // Simple DTO to parse user-service response
//    public record UserDto(String id, String name, String email) { }
//}
