package com.example.chatbotservice.booking;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PendingBookingRedisService {

    private static final String KEY_PREFIX =
            "chat:pending-booking:";

    private static final String BOOKING_LINK_PREFIX =
            "chat:booking-to-conversation:";

    private static final Duration BOOKING_TTL =
            Duration.ofMinutes(10);

    private static final Duration BOOKING_LINK_TTL =
            Duration.ofHours(2);

    private final RedisTemplate<String, String> redisTemplate;

    private final ObjectMapper objectMapper;

    public PendingBookingRedisService(
            RedisTemplate<String, String> redisTemplate,
            ObjectMapper objectMapper
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Save pending booking context for confirmation.
     */
    public void save(
            PendingBookingContext context
    ) {

        try {

            String key = buildKey(context.conversationId());

            String json =
                    objectMapper.writeValueAsString(
                            context
                    );

            redisTemplate.opsForValue()
                    .set(
                            key,
                            json,
                            BOOKING_TTL
                    );

        } catch (Exception ex) {

            throw new IllegalStateException(
                    "Failed to save pending booking",
                    ex
            );
        }
    }

    /**
     * Load pending booking context.
     */
    public Optional<PendingBookingContext> find(
            String conversationId
    ) {

        try {

            String key =
                    buildKey(conversationId);

            String value =
                    redisTemplate.opsForValue().get(key);

            if (value == null || value.isBlank()) {

                return Optional.empty();
            }

            return Optional.of(
                    objectMapper.readValue(
                            value,
                            PendingBookingContext.class
                    )
            );

        } catch (Exception ex) {

            throw new IllegalStateException(
                    "Failed to load pending booking context",
                    ex
            );
        }
    }

    /**
     * Remove pending booking after:
     * - successful booking
     * - cancellation
     */
    public void delete(
            String conversationId
    ) {

        redisTemplate.delete(
                buildKey(conversationId)
        );
    }

    /**
     * Check whether a booking is waiting
     * for confirmation.
     */
    public boolean exists(
            String conversationId
    ) {

        Boolean exists =
                redisTemplate.hasKey(
                        buildKey(conversationId)
                );

        return Boolean.TRUE.equals(exists);
    }

    private String buildKey(
            String conversationId
    ) {

        return KEY_PREFIX + conversationId;
    }

    public void updateState(
            String conversationId,
            BookingWorkflowState newState
    ) {

        Optional<PendingBookingContext> existing =
                find(conversationId);

        if (existing.isEmpty()) {

            return;
        }

        PendingBookingContext current =
                existing.get();

        PendingBookingContext updated =
                new PendingBookingContext(

                        current.conversationId(),

                        current.userId(),

                        current.type(),

                        current.bookingData(),

                        current.selectedData(),

                        newState,

                        current.paymentMethod(),

                        current.createdAt()
                );

        save(updated);
    }

    public void updateBookingData(
            String conversationId,
            String bookingData
    ) {

        Optional<PendingBookingContext> contextOptional =
                find(conversationId);

        if (contextOptional.isEmpty()) {

            throw new IllegalStateException(
                    "Pending booking not found for conversation: "
                            + conversationId
            );
        }

        PendingBookingContext existing =
                contextOptional.get();

        PendingBookingContext updated =
                new PendingBookingContext(

                        existing.conversationId(),

                        existing.userId(),

                        existing.type(),

                        bookingData,

                        existing.selectedData(),

                        existing.state(),

                        existing.paymentMethod(),

                        existing.createdAt()
                );

        save(updated);
    }

    /**
     * Persist the payment method the user chose in the
     * AWAITING_PAYMENT_METHOD step.
     */
    public void updatePaymentMethod(
            String conversationId,
            String paymentMethod
    ) {

        Optional<PendingBookingContext> contextOptional =
                find(conversationId);

        if (contextOptional.isEmpty()) {

            throw new IllegalStateException(
                    "Pending booking not found for conversation: "
                            + conversationId
            );
        }

        PendingBookingContext existing =
                contextOptional.get();

        PendingBookingContext updated =
                new PendingBookingContext(

                        existing.conversationId(),

                        existing.userId(),

                        existing.type(),

                        existing.bookingData(),

                        existing.selectedData(),

                        existing.state(),

                        paymentMethod,

                        existing.createdAt()
                );

        save(updated);
    }

    // =====================================================
    // bookingUid -> conversationId  (needed by the Kafka listener:
    // BookingConfirmedEvent/BookingFailedEvent only carry bookingUid,
    // not conversationId, so we look it up here)
    // =====================================================

    public void linkBookingToConversation(
            String bookingUid,
            String conversationId
    ) {
        redisTemplate.opsForValue().set(
                BOOKING_LINK_PREFIX + bookingUid,
                conversationId,
                BOOKING_LINK_TTL
        );
    }

    public Optional<String> findConversationIdForBooking(
            String bookingUid
    ) {
        String conversationId =
                redisTemplate.opsForValue().get(BOOKING_LINK_PREFIX + bookingUid);

        return Optional.ofNullable(conversationId);
    }


}
