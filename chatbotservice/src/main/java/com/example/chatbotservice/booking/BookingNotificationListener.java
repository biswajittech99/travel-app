package com.example.chatbotservice.booking;

import com.example.chatbotservice.rag.memory.ConversationMemoryService;
import org.example.common.dto.BookingConfirmedEvent;
import org.example.common.dto.BookingFailedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BookingNotificationListener {

    private static final Logger log =
            LoggerFactory.getLogger(BookingNotificationListener.class);

    private final PendingBookingRedisService pendingBookingRedisService;
    private final ConversationMemoryService memoryService;

    public BookingNotificationListener(
            PendingBookingRedisService pendingBookingRedisService,
            ConversationMemoryService memoryService
    ) {
        this.pendingBookingRedisService = pendingBookingRedisService;
        this.memoryService = memoryService;
    }

    @KafkaListener(
            topics = "booking-confirmed-topic",
            groupId = "chatbot-notification-group",
            containerFactory = "bookingConfirmedKafkaListenerFactory"
    )
    public void onBookingConfirmed(BookingConfirmedEvent event) {

        log.info("📩 booking-confirmed-topic received for booking {}",
                event.bookingUid());

        Optional<String> conversationId =
                pendingBookingRedisService.findConversationIdForBooking(
                        event.bookingUid()
                );

        if (conversationId.isEmpty()) {
            log.warn(
                    "No conversation mapped for confirmed booking {}. " +
                            "It resolved after the mapping expired, or the " +
                            "chatbot restarted without persisting it.",
                    event.bookingUid()
            );
            return;
        }

        pendingBookingRedisService.updateState(
                conversationId.get(),
                BookingWorkflowState.CONFIRMED
        );

        String message = """
                ✅ Your booking is confirmed!

                Booking ID: %s
                PNR: %s
                Total paid: %.2f

                Have a great trip!
                """.formatted(
                event.bookingUid(),
                event.pnr(),
                event.totalAmount()
        );

        memoryService.addAssistantMessage(conversationId.get(), message);
    }

    @KafkaListener(
            topics = "booking-failed-topic",
            groupId = "chatbot-notification-group",
            containerFactory = "bookingFailedKafkaListenerFactory"
    )
    public void onBookingFailed(BookingFailedEvent event) {

        log.info("📩 booking-failed-topic received for booking {}: {}",
                event.bookingUid(), event.reason());

        Optional<String> conversationId =
                pendingBookingRedisService.findConversationIdForBooking(
                        event.bookingUid()
                );

        if (conversationId.isEmpty()) {
            log.warn(
                    "No conversation mapped for failed booking {}.",
                    event.bookingUid()
            );
            return;
        }

        pendingBookingRedisService.updateState(
                conversationId.get(),
                BookingWorkflowState.FAILED
        );

        String message = """
                ❌ Sorry, your payment could not be completed.

                Booking ID: %s
                Reason: %s

                You can try booking again whenever you're ready.
                """.formatted(
                event.bookingUid(),
                event.reason()
        );

        memoryService.addAssistantMessage(conversationId.get(), message);
    }

}
