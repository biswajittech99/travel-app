package com.example.chatbotservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;
import java.util.function.Supplier;

@Service
public class ToolExecutorService {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${services.booking-url}")
    private String bookingUrl;

    @Value("${services.user-url}")
    private String userUrl;

    @Value("${services.search-url}")
    private String searchUrl;

    @Value("${chatbot.cache.booking-ttl}")
    private long bookingTtl;

    @Value("${chatbot.cache.flight-ttl}")
    private long flightTtl;

    private final RedisTemplate<String, Object> redisTemplate;
    private static final Logger log =
            LoggerFactory.getLogger(ToolExecutorService.class);

    public ToolExecutorService(WebClient.Builder webClientBuilder,
                               ObjectMapper objectMapper,
                               RedisTemplate<String, Object> redisTemplate) {
        this.webClientBuilder = webClientBuilder;
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
    }

    public String execute(String toolName,
                              Map<String, Object> input,
                              String userId) {
        try {
            return switch (toolName) {
                case "get_user_bookings" -> getCached(
                        "bookings:" + input.get("user_id"),
                        () -> getUserBookings((String) input.get("user_id")),
                        bookingTtl);

                case "get_booking_details" -> getCached(
                        "booking:" + input.get("booking_id"),
                        () -> getBookingDetails((String) input.get("booking_id")),
                        bookingTtl);

                case "search_flights" ->
                        searchFlights(
                                (String) input.get("origin"),
                                (String) input.get("destination"),
                                (String) input.get("date"));

                case "search_hotels" ->
                        searchHotels(
                                (String) input.get("city"),
                                (String) input.get("checkIn"),
                                (String) input.get("checkOut"),
                                (Integer) input.get("adults"),
                                (Integer) input.get("rooms")
                        );

                case "get_user_profile" -> getCached(
                        "profile:" + input.get("user_id"),
                        () -> getUserProfile((String) input.get("user_id")),
                        3600L);

                case "get_flight_status" ->
                        getFlightStatus((String) input.get("flight_number"));

                default -> "Tool not found: " + toolName;
            };
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String getCached(String key,
                             Supplier<String> fetcher,
                             long ttlSeconds) {
        try {
            Object cached = redisTemplate.opsForValue()
                    .get("chatbot:" + key);
            if (cached != null) {
                return cached.toString();
            }
            String result = fetcher.get();
            redisTemplate.opsForValue().set(
                    "chatbot:" + key, result,
                    Duration.ofSeconds(ttlSeconds));
            return result;
        } catch (Exception e) {
            return fetcher.get();
        }
    }

    private String getUserBookings(String userId) {
        try {
            return webClientBuilder.build().get()
                    .uri(bookingUrl + "/api/bookings/user/" + userId)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            return "{\"error\": \"Could not fetch bookings\"}";
        }
    }

    private String getBookingDetails(String bookingId) {
        try {
            return webClientBuilder.build().get()
                    .uri(bookingUrl + "/api/bookings/" + bookingId)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            return "{\"error\": \"Could not fetch booking details\"}";
        }
    }

    private String searchFlights(
            String origin,
            String destination,
            String date) {

        try {

            log.info(
                    "Searching flights: {} -> {} on {}",
                    origin,
                    destination,
                    date
            );

            String url = searchUrl + "/api/search/flights";

            log.info("Calling Search Service URL: {}", url);

            return webClientBuilder
                    .build()
                    .get()
                    .uri(
                            url,
                            uriBuilder -> uriBuilder
                                    .queryParam("origin", origin)
                                    .queryParam("destination", destination)
                                    .queryParam("date", date)
                                    .build()
                    )
                    .retrieve()

                    .onStatus(
                            status -> status.isError(),
                            response -> response
                                    .bodyToMono(String.class)
                                    .map(body -> {

                                        log.error(
                                                "Search Service returned HTTP {}: {}",
                                                response.statusCode(),
                                                body
                                        );

                                        return new RuntimeException(
                                                "Search Service error: "
                                                        + response.statusCode()
                                                        + " - "
                                                        + body
                                        );
                                    })
                    )

                    .bodyToMono(String.class)
                    .block();

        } catch (Exception e) {

            log.error(
                    "Flight search failed: {} -> {} on {}",
                    origin,
                    destination,
                    date,
                    e
            );

            return """
                {
                  "error": "Could not search flights"
                }
                """;
        }
    }


    private String searchHotels(
            String city,
            String checkIn,
            String checkOut,
            Integer adults,
            Integer rooms) {

        try {

            log.info(
                    "Searching hotels: {} from {} to {}, adults={}, rooms={}",
                    city,
                    checkIn,
                    checkOut,
                    adults,
                    rooms
            );

            String url = searchUrl + "/api/search/hotels";

            log.info(
                    "Calling Search Service URL: {}",
                    url
            );

            return webClientBuilder
                    .build()
                    .get()
                    .uri(
                            url,
                            uriBuilder -> uriBuilder
                                    .queryParam("city", city)
                                    .queryParam("checkIn", checkIn)
                                    .queryParam("checkOut", checkOut)
                                    .queryParam("adults", adults)
                                    .queryParam("rooms", rooms)
                                    .build()
                    )
                    .retrieve()

                    .onStatus(
                            status -> status.isError(),
                            response -> response
                                    .bodyToMono(String.class)
                                    .map(body -> {

                                        log.error(
                                                "Search Service returned HTTP {}: {}",
                                                response.statusCode(),
                                                body
                                        );

                                        return new RuntimeException(
                                                "Search Service error: "
                                                        + response.statusCode()
                                                        + " - "
                                                        + body
                                        );
                                    })
                    )

                    .bodyToMono(String.class)
                    .block();

        } catch (Exception e) {

            log.error(
                    "Hotel search failed: {} from {} to {}",
                    city,
                    checkIn,
                    checkOut,
                    e
            );

            return """
                {
                  "error": "Could not search hotels"
                }
                """;
        }
    }

    private String getUserProfile(String userId) {
        try {
            return webClientBuilder.build().get()
                    .uri(userUrl + "/api/users/" + userId)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            return "{\"error\": \"Could not fetch profile\"}";
        }
    }

    private String getFlightStatus(String flightNumber) {
        try {
            return webClientBuilder.build().get()
                    .uri(bookingUrl + "/api/bookings/flight-status/"
                            + flightNumber)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            return "{\"error\": \"Could not fetch flight status\"}";
        }
    }
}
