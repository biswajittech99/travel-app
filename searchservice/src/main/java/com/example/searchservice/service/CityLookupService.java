//package com.example.searchservice.service;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import org.springframework.stereotype.Service;
//import org.springframework.web.reactive.function.client.WebClient;
//import reactor.core.publisher.Mono;
//
//@Service
//public class CityLookupService {
//
//    private final WebClient webClient;
//    private final AmadeusAuthService authService;
//
//    public CityLookupService(WebClient.Builder builder,
//                             AmadeusAuthService authService) {
//
//        this.webClient = builder
//                .baseUrl("https://test.api.amadeus.com")
//                .build();
//
//        this.authService = authService;
//    }
//
//    public Mono<String> findCityCode(String cityName) {
//
//        return authService.getAccessToken()
//
//                .flatMap(token ->
//
//                        webClient.get()
//
//                                .uri(uri -> uri
//
//                                        .path("/v1/reference-data/locations")
//
//                                        .queryParam("subType", "CITY")
//
//                                        .queryParam("keyword", cityName)
//
//                                        .queryParam("page[limit]", 1)
//
//                                        .build())
//
//                                .headers(h ->
//                                        h.setBearerAuth(token))
//
//                                .retrieve()
//
//                                .bodyToMono(JsonNode.class)
//
//                                .map(this::extractCityCode)
//
//                );
//
//    }
//
//    private String extractCityCode(JsonNode root) {
//
//        if (root == null ||
//                !root.has("data") ||
//                root.get("data").isEmpty()) {
//
//            throw new RuntimeException("City not found");
//        }
//
//        return root.get("data")
//                .get(0)
//                .get("iataCode")
//                .asText();
//    }
//
//}
