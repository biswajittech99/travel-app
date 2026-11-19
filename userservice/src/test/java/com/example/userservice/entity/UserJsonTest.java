package com.example.userservice.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class UserJsonTest {

    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    private User sampleUser() {
        return new User("id-1", "Test User", "test@example.com",
                LocalDateTime.now(), Role.USER, "$2a$10$somehashvalue", true);
    }

    @Test
    void passwordIsNeverSerialized() throws Exception {
        String json = mapper.writeValueAsString(sampleUser());
        JsonNode node = mapper.readTree(json);

        assertFalse(node.has("password"), "password must not appear in JSON responses");
        assertFalse(json.contains("somehashvalue"), "password value must not leak");
    }

    @Test
    void passwordCanStillBeReadFromRequestBody() throws Exception {
        String body = "{\"name\":\"A\",\"email\":\"a@example.com\",\"password\":\"secret123\"}";
        User user = mapper.readValue(body, User.class);

        assertEquals("secret123", user.getPassword());
    }

    @Test
    void otherFieldsAreStillSerialized() throws Exception {
        JsonNode node = mapper.readTree(mapper.writeValueAsString(sampleUser()));

        assertEquals("test@example.com", node.get("email").asText());
        assertEquals("Test User", node.get("name").asText());
    }
}
