//package com.example.searchservice.service;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.MediaType;
//import org.springframework.stereotype.Service;
//import org.springframework.web.reactive.function.BodyInserters;
//import org.springframework.web.reactive.function.client.WebClient;
//import reactor.core.publisher.Mono;
//
//import java.time.Instant;
//
//@Service
//public class AmadeusAuthService {
//
//    private final WebClient webClient;
//    private final String clientId;
//    private final String clientSecret;
//
//    // cached token and expiry
//    private volatile String cachedToken;
//    private volatile Instant tokenExpiry;
//    private final Object lock = new Object();
//
//    public AmadeusAuthService(WebClient.Builder builder,
//                              @Value("${amadeus.base-url}") String baseUrl,
//                              @Value("${amadeus.client-id}") String clientId,
//                              @Value("${amadeus.client-secret}") String clientSecret) {
//        this.webClient = builder.baseUrl(baseUrl).build();
//        this.clientId = clientId;
//        this.clientSecret = clientSecret;
//    }
//
//    public Mono<String> getAccessToken() {
//        // quick path: token present and not about to expire (30s buffer)
//        if (cachedToken != null && Instant.now().isBefore(tokenExpiry.minusSeconds(30))) {
//            return Mono.just(cachedToken);
//        }
//
//        // synchronize to avoid stampede
//        synchronized (lock) {
//            if (cachedToken != null && Instant.now().isBefore(tokenExpiry.minusSeconds(30))) {
//                return Mono.just(cachedToken);
//            }
//
//            // request a fresh token
//            return webClient.post()
//                    .uri("/v1/security/oauth2/token")
//                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
//                    .body(BodyInserters.fromFormData("grant_type", "client_credentials")
//                            .with("client_id", clientId)
//                            .with("client_secret", clientSecret))
//                    .retrieve()
//                    .bodyToMono(JsonNode.class)
//                    .map(json -> {
//                        String token = json.get("access_token").asText();
//                        int expiresIn = json.has("expires_in") ? json.get("expires_in").asInt() : 1700;
//                        cachedToken = token;
//                        tokenExpiry = Instant.now().plusSeconds(expiresIn);
//                        return token;
//                    });
//        }
//    }
//}
