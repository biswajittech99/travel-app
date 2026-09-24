package com.example.chatbotservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ClaudeAiService {

    private final WebClient webClient;
    private final ToolExecutorService toolExecutor;
    private final ObjectMapper objectMapper;

    @Value("${anthropic.api.key}")
    private String apiKey;

    @Value("${anthropic.api.model}")
    private String model;

    @Value("${anthropic.api.max-tokens}")
    private int maxTokens;

    public ClaudeAiService(WebClient webClient,
                           ToolExecutorService toolExecutor,
                           ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.toolExecutor = toolExecutor;
        this.objectMapper = objectMapper;
    }

    public String chat(String userId, List<Map<String, Object>> messages) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("max_tokens", maxTokens);
            requestBody.put("tools", getTools());
            requestBody.put("system", buildSystemPrompt(userId));
            requestBody.put("messages", messages);

            Map response = webClient.post()
                    .uri("https://api.anthropic.com/v1/messages")
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", "2023-06-01")
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            return handleResponse(userId, messages, response);

        } catch (Exception e) {
            return "Sorry, I encountered an error: " + e.getMessage();
        }
    }

    private String handleResponse(String userId,
                                  List<Map<String, Object>> messages,
                                  Map response) throws Exception {
        List<Map<String, Object>> content =
                (List<Map<String, Object>>) response.get("content");
        String stopReason = (String) response.get("stop_reason");

        // ✅ Claude wants to use a tool
        if ("tool_use".equals(stopReason)) {
            messages.add(Map.of("role", "assistant", "content", content));

            List<Map<String, Object>> toolResults = new ArrayList<>();
            for (Map<String, Object> block : content) {
                if ("tool_use".equals(block.get("type"))) {
                    String toolName = (String) block.get("name");
                    String toolUseId = (String) block.get("id");
                    Map<String, Object> input =
                            (Map<String, Object>) block.get("input");

                    String result = toolExecutor.execute(
                            toolName, input, userId);

                    toolResults.add(Map.of(
                            "type", "tool_result",
                            "tool_use_id", toolUseId,
                            "content", result
                    ));
                }
            }

            messages.add(Map.of("role", "user", "content", toolResults));
            return chat(userId, messages);  // recursive with tool results
        }

        // ✅ Final text response
        for (Map<String, Object> block : content) {
            if ("text".equals(block.get("type"))) {
                return (String) block.get("text");
            }
        }

        return "I couldn't process your request.";
    }

    private List<Map<String, Object>> getTools() {
        return List.of(
                buildTool("get_user_bookings",
                        "Get all flight bookings for a user.",
                        Map.of("type", "object",
                                "properties", Map.of(
                                        "user_id", Map.of("type", "string",
                                                "description", "The user ID")),
                                "required", List.of("user_id"))),

                buildTool("get_booking_details",
                        "Get detailed information about a specific booking.",
                        Map.of("type", "object",
                                "properties", Map.of(
                                        "booking_id", Map.of("type", "string",
                                                "description", "The booking ID")),
                                "required", List.of("booking_id"))),

                buildTool("search_flights",
                        "Search for available flights.",
                        Map.of("type", "object",
                                "properties", Map.of(
                                        "origin", Map.of("type", "string",
                                                "description", "Origin airport code e.g DEL"),
                                        "destination", Map.of("type", "string",
                                                "description", "Destination airport code e.g BOM"),
                                        "date", Map.of("type", "string",
                                                "description", "Travel date YYYY-MM-DD")),
                                "required", List.of("origin", "destination", "date"))),

                buildTool("get_user_profile",
                        "Get user profile information.",
                        Map.of("type", "object",
                                "properties", Map.of(
                                        "user_id", Map.of("type", "string",
                                                "description", "The user ID")),
                                "required", List.of("user_id"))),

                buildTool("get_flight_status",
                        "Get current flight status including delays.",
                        Map.of("type", "object",
                                "properties", Map.of(
                                        "flight_number", Map.of("type", "string",
                                                "description", "Flight number e.g AI-202")),
                                "required", List.of("flight_number")))
        );
    }

    private Map<String, Object> buildTool(String name,
                                          String description,
                                          Map<String, Object> inputSchema) {
        return Map.of(
                "name", name,
                "description", description,
                "input_schema", inputSchema
        );
    }

    private String buildSystemPrompt(String userId) {
        return """
            You are TravelBot, a helpful AI assistant for TravelApp.
            Current user ID: %s
            
            You help users with:
            - Checking their flight bookings and details
            - Searching for available flights
            - Checking flight status and delays
            - Viewing profile information
            
            Be friendly, concise, and use emojis.
            When user says 'my bookings', use their user ID automatically.
            Format responses clearly for easy reading.
            """.formatted(userId);
    }
}
