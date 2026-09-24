package com.example.chatbotservice.booking.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class UserServiceClient {

    private static final Logger log = LoggerFactory.getLogger(UserServiceClient.class);

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public UserServiceClient(
            WebClient.Builder builder,
            ObjectMapper objectMapper,
            @Value("${services.user-url}") String userUrl
    ) {
        this.webClient = builder.baseUrl(userUrl).build();
        this.objectMapper = objectMapper;
    }

    /**
     * @return the user's email, or null if it could not be resolved.
     * Callers must treat null as "cannot proceed with booking" — never
     * substitute a placeholder email, since PaymentRequest/Booking both
     * rely on it being a real, contactable address.
     */
    public String getUserEmail(String userId) {
        try {
            String raw = webClient.get()
                    .uri("/api/users/" + userId)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (raw == null || raw.isBlank()) {
                return null;
            }

            JsonNode node = objectMapper.readTree(raw);
            JsonNode emailNode = node.has("email") ? node.get("email") : null;

            return emailNode == null || emailNode.isNull()
                    ? null
                    : emailNode.asText();

        } catch (Exception e) {
            log.error("Failed to fetch email for userId={}", userId, e);
            return null;
        }
    }
}
