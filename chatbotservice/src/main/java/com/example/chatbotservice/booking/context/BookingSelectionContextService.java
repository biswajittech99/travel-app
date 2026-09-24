package com.example.chatbotservice.booking.context;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
public class BookingSelectionContextService {
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final Duration CONTEXT_TTL =
            Duration.ofMinutes(15);

    public BookingSelectionContextService(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    private String flightKey(
            String userId,
            String conversationId
    ) {
        return "chat:booking:flight:"
                + userId + ":" + conversationId;
    }

    private String hotelKey(
            String userId,
            String conversationId
    ) {
        return "chat:booking:hotel:"
                + userId + ":" + conversationId;
    }

    // =====================================================
    // FLIGHT
    // =====================================================

    public void saveFlightSelection(
            String userId,
            String conversationId,
            SelectedFlightContext context
    ) {

        try {

            redisTemplate.opsForValue().set(
                    flightKey(userId, conversationId),
                    objectMapper.writeValueAsString(context),
                    CONTEXT_TTL
            );

        } catch (Exception ex) {

            throw new RuntimeException(
                    "Failed to save selected flight",
                    ex
            );
        }
    }

    public Optional<SelectedFlightContext> getFlightSelection(
            String userId,
            String conversationId
    ) {

        try {

            String json =
                    redisTemplate.opsForValue().get(
                            flightKey(
                                    userId,
                                    conversationId
                            )
                    );

            if (json == null) {
                return Optional.empty();
            }

            return Optional.of(
                    objectMapper.readValue(
                            json,
                            SelectedFlightContext.class
                    )
            );

        } catch (Exception ex) {

            throw new RuntimeException(
                    "Failed to load selected flight",
                    ex
            );
        }
    }
    // =====================================================
    // HOTEL
    // =====================================================

    public void saveHotelSelection(
            String userId,
            String conversationId,
            SelectedHotelContext context
    ) {

        try {

            redisTemplate.opsForValue().set(
                    hotelKey(userId, conversationId),
                    objectMapper.writeValueAsString(context),
                    CONTEXT_TTL
            );

        } catch (Exception ex) {

            throw new RuntimeException(
                    "Failed to save selected hotel",
                    ex
            );
        }
    }

    public Optional<SelectedHotelContext> getHotelSelection(
            String userId,
            String conversationId
    ) {

        try {

            String json =
                    redisTemplate.opsForValue().get(
                            hotelKey(
                                    userId,
                                    conversationId
                            )
                    );

            if (json == null) {
                return Optional.empty();
            }

            return Optional.of(
                    objectMapper.readValue(
                            json,
                            SelectedHotelContext.class
                    )
            );

        } catch (Exception ex) {

            throw new RuntimeException(
                    "Failed to load selected hotel",
                    ex
            );
        }
    }

    // =====================================================
    // DELETE
    // =====================================================

    public void clearFlightSelection(
            String userId,
            String conversationId
    ) {

        redisTemplate.delete(
                flightKey(
                        userId,
                        conversationId
                )
        );
    }

    public void clearHotelSelection(
            String userId,
            String conversationId
    ) {

        redisTemplate.delete(
                hotelKey(
                        userId,
                        conversationId
                )
        );
    }
}
